package am5800.harmonie.app.model.features.flow

class CategoryDistribution(distribution: Map<FlowItemCategory, Double>) {
  private val summedDistribution = mutableListOf<Pair<Double, FlowItemCategory>>()

  init {
    var sum = 0.0
    for ((category, p) in distribution) {
      sum += p
      summedDistribution.add(Pair(sum, category))
    }
    if (Math.abs(sum - 1.0) > 0.001) throw Exception("Distribution does not sum to one!")
  }

  fun getCategory(p: Double): FlowItemCategory {
    if (p < 0.0 || p > 1.0) throw Exception("P should be in range [0;1]")

    return summedDistribution.first { p < it.first }.second
  }
}