package utils

import java.util.*

class PropertyChangedArg<T>(private val old: T, val newValue: T?, val hasOld: Boolean) {
  val oldValue: T
    get() = if (!hasOld) throw Exception("Old value does not exist") else old

  var handled = false
}

class Property<T>(lifetime: Lifetime, value: T) {
  init {
    lifetime.addAction { binders.clear() }
  }
  private val binders: ArrayList<(PropertyChangedArg<T?>) -> Unit> = ArrayList()
  private val valueLifetime = SequentialLifetime(lifetime)
  var value: T? = value
    get() = field
    set(v) {
      if (field == v) return
      valueLifetime.next()
      val arg = PropertyChangedArg(field, v, true)
      field = v
      binders.forEach { b -> if (!arg.handled) b(arg) }
    }

  fun bind(lifetime: Lifetime, binder: (PropertyChangedArg<T?>) -> Unit) {
    binders.add(binder)
    lifetime.addAction { binders.remove (binder) }
    binder(PropertyChangedArg(null, value, false))
  }

  fun bindNotNull(lifetime: Lifetime, binder: (T) -> Unit) {
    bind(lifetime, { arg -> if (arg.newValue != null) binder(arg.newValue) })
  }

  fun bindHasOld(lifetime: Lifetime, binder: (T?) -> Unit) {
    bind(lifetime, { arg ->
      if (arg.hasOld) binder(arg.newValue)
    })
  }

  fun forEachValue(lifetime: Lifetime, binder: (T, Lifetime) -> Unit) {
    bindNotNull(lifetime, { args ->
      binder(args, valueLifetime.current!!)
    })
  }
}