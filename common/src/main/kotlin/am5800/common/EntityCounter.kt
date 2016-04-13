package am5800.common


class WithCounter<T>(val entity: T, val count: Int) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as WithCounter<*>

    if (entity != other.entity) return false

    return true
  }

  override fun hashCode(): Int {
    return entity?.hashCode() ?: 0
  }
}

class EntityCounter<T> {
  private val _result = mutableMapOf<T, Int>()

  val result: Map<T, Int>
    get() = _result

  fun add(entity: T, delta: Int = 1) {
    if (delta <= 0) return
    val previous = _result[entity] ?: 0
    _result[entity] = previous + delta
  }

  fun add(withCounters: Collection<WithCounter<T>>) {
    for (countable in withCounters) {
      add(countable.entity, countable.count)
    }
  }
}

fun <T> Collection<WithCounter<T>>.merge(): Collection<WithCounter<T>> {
  return this.groupBy { it.entity }.map { grouping -> WithCounter(grouping.key, grouping.value.sumBy { it.count }) }
}