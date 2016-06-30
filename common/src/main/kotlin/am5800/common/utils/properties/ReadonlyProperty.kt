package am5800.common.utils.properties

import am5800.common.utils.Lifetime

interface ReadonlyProperty<out T : Any> {
  val value: T
  fun onChange(lifetime: Lifetime, handler: (PropertyChangedArg<T>) -> Unit)
  fun forEachValue(lifetime: Lifetime, handler: (T, Lifetime) -> Unit)
}