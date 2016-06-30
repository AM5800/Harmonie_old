package am5800.common.utils.properties

class PropertyChangedArg<out T : Any>(val old: T?, val newValue: T) {
  val isAcknowledge: Boolean = old == null
}

class NullablePropertyChangedArg<out T : Any>(val old: T?, val newValue: T?, val hasOld: Boolean) {
  val isAcknowledge: Boolean = !hasOld
}