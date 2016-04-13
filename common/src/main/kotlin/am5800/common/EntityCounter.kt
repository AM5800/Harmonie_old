package am5800.common


class EntityCounter<T> {
  private val _result = mutableMapOf<T, Int>()

  val result: Map<T, Int>
    get() = _result

  fun add(entity: T) {
    val previous = _result[entity] ?: 0
    _result[entity] = previous + 1
  }
}