package am5800.common.utils

import java.util.*

class EnumerableDistribution<out T>(distribution: Map<T, Double>) {
  private val summedDistribution = mutableListOf<Pair<Double, T>>()

  val items : List<T>
    get() = summedDistribution.map { it.second }

  init {
    var sum = 0.0
    for ((category, p) in distribution) {
      sum += p
      summedDistribution.add(Pair(sum, category))
    }
    if (Math.abs(sum - 1.0) > 0.001) throw Exception("Distribution does not sum to one!")
  }

  fun get(p: Double): T {
    if (p < 0.0 || p > 1.0) throw Exception("P should be in range [0;1]")

    return summedDistribution.first { p < it.first }.second
  }

  fun get(random: Random): T {
    return get(random.nextDouble())
  }

  class EnumerableDistributionDefinition<T> {
    val map = mutableMapOf<T, Double>()

    fun add(value: T, probability: Double) {
      if (map.containsKey(value)) throw Exception("Duplicate value: $value")
      if (probability < 0 || probability > 1) throw Exception("Value out of range: $probability")

      map.put(value, probability)
    }

    fun addRest(value: T) {
      val sum = map.toList().sumByDouble { it.second }
      if (sum > 1) throw Exception("Rest is negative")

      add(value, 1.0 - sum)
    }

    fun equal(values: Collection<T>) {
      val delta = 1.0 / values.size
      for (value in values) {
        add(value, delta)
      }
    }
  }

  companion object {
    fun <T> define(init: EnumerableDistributionDefinition<T>.() -> Unit): EnumerableDistribution<T> {
      val builder = EnumerableDistributionDefinition<T>()
      init(builder)
      return EnumerableDistribution(builder.map)
    }

  }
}