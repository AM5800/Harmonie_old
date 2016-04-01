package am5800.common.utils

import java.util.*

interface ReadonlyProperty<T : Any> {
  val value: T?
  fun bind(lifetime: Lifetime, binder: (PropertyChangedArg<T?>) -> Unit)
  fun bind(lifetime: Lifetime, sourceProperty: Property<T>)
  fun bindNotNull(lifetime: Lifetime, binder: (T) -> Unit)
  fun forEachValue(lifetime: Lifetime, binder: (T?, Lifetime) -> Unit)
}

class PropertyChangedArg<T>(private val old: T?, val newValue: T?, val hasOld: Boolean) {
  val oldValue: T?
    get() = if (hasOld) old else throw Exception("Old value is not available")
}

class Property<T : Any>(lifetime: Lifetime, initialValue: T?) : ReadonlyProperty<T> {
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
      binders.forEach { b -> b(arg) }
    }

  override fun bind(lifetime: Lifetime, binder: (PropertyChangedArg<T?>) -> Unit) {
    binders.add(binder)
    lifetime.addAction { binders.remove (binder) }
    binder(PropertyChangedArg(null, value, false))
  }

  override fun bind(lifetime: Lifetime, sourceProperty: Property<T>) {
    sourceProperty.bind(lifetime, {
      value = it.newValue
    })
  }

  override fun bindNotNull(lifetime: Lifetime, binder: (T) -> Unit) {
    bind(lifetime, { arg -> if (arg.newValue != null) binder(arg.newValue) })
  }

  override fun forEachValue(lifetime: Lifetime, binder: (T?, Lifetime) -> Unit) {
    bindNotNull(lifetime, { args ->
      binder(args, valueLifetime.current)
    })
  }
}