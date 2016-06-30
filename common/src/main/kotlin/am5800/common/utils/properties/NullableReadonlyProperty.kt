package am5800.common.utils.properties

import am5800.common.utils.Lifetime

interface NullableReadonlyProperty<out T : Any> {
  val value: T?
  fun forEachValue(lifetime: Lifetime, handler: (T?, Lifetime) -> Unit)
  fun onChange(lifetime: Lifetime, handler: (NullablePropertyChangedArg<T>) -> Unit)
}