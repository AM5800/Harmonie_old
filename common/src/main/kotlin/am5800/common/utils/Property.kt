package am5800.common.utils

import java.util.*

interface ReadonlyProperty<T : Any> {
  val value: T?
  fun onChange(lifetime: Lifetime, binder: (PropertyChangedArg<T?>) -> Unit)
  fun onChange(lifetime: Lifetime, targetProperty: Property<T>)
  fun onChangeNotNull(lifetime: Lifetime, binder: (T) -> Unit)
  fun forEachValue(lifetime: Lifetime, binder: (T?, Lifetime) -> Unit)
  fun onValue(lifetime: Lifetime, value: T?, callback: () -> Unit)
}

class PropertyChangedArg<T>(private val old: T?, val newValue: T?, val hasOld: Boolean) {
  val oldValue: T?
    get() = if (hasOld) old else throw Exception("Old value is not available")
  val isAcknowledge: Boolean = hasOld == false
}

class Property<T : Any>(val lifetime: Lifetime, initialValue: T?) : ReadonlyProperty<T> {
  override fun onValue(lifetime: Lifetime, value: T?, callback: () -> Unit) {
    onChange(lifetime, { args ->
      if (args.newValue == value) callback()
    })
  }

  init {
    lifetime.addAction { binders.clear() }
  }

  private val binders: ArrayList<(PropertyChangedArg<T?>) -> Unit> = ArrayList()
  private val valueLifetime = SequentialLifetime(lifetime)

  override var value: T? = initialValue
    get() = field
    set(newValue) {
      if (field == newValue) return
      valueLifetime.next()
      val arg: PropertyChangedArg<T?> = PropertyChangedArg(field, newValue, true)
      field = newValue
      binders.toList().forEach { b -> b(arg) }
    }

  override fun onChange(lifetime: Lifetime, binder: (PropertyChangedArg<T?>) -> Unit) {
    binders.add(binder)
    lifetime.addAction { binders.remove (binder) }
    binder(PropertyChangedArg(null, value, false))
  }

  override fun onChange(lifetime: Lifetime, targetProperty: Property<T>) {
    this.onChange(lifetime, {
      targetProperty.value = it.newValue
    })
  }

  override fun onChangeNotNull(lifetime: Lifetime, binder: (T) -> Unit) {
    onChange(lifetime, { arg -> if (arg.newValue != null) binder(arg.newValue) })
  }

  override fun forEachValue(lifetime: Lifetime, binder: (T?, Lifetime) -> Unit) {
    onChangeNotNull(lifetime, { args ->
      binder(args, valueLifetime.current)
    })
  }
}

fun <TSrc : Any, TDst : Any> Property<TSrc>.convert(srcDst: (TSrc?) -> TDst?, dstSrc: (TDst?) -> TSrc?): Property<TDst> {
  val result = Property<TDst>(this.lifetime, null)
  onChange(this.lifetime, { result.value = srcDst(it.newValue) })
  result.onChange(this.lifetime, { value = dstSrc(it.newValue) })
  return result
}