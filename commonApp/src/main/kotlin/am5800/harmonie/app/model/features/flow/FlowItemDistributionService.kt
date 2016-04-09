package am5800.harmonie.app.model.features.flow

class FlowItemDistributionService(private val providers: Collection<FlowItemProvider>) {
  fun getDistribution(): CategoryDistribution {
    val allCategories = providers.flatMap { it.supportedCategories }.distinct()
    return CategoryDistribution(allCategories.map { Pair(it, 1.0 / allCategories.size) }.toMap())
  }
}