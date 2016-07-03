package am5800.common.utils.properties

import am5800.common.utils.Lifetime

class ConstantProperty<out T : Any>(override val value: T, private val lifetime: Lifetime) : ReadonlyProperty<T> {
  override fun onChange(lifetime: Lifetime, handler: (PropertyChangedArg<T>) -> Unit) {
    handler(PropertyChangedArg(null, value))
  }

  override fun forEachValue(lifetime: Lifetime, handler: (T, Lifetime) -> Unit) {
    handler(value, lifetime)
  }
}