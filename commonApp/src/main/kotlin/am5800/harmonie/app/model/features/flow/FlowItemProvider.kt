package am5800.harmonie.app.model.features.flow

interface FlowItemProvider {
  val supportedCategories: Set<FlowItemCategory>
  fun getAvailableDataSetSize(category: FlowItemCategory): Int
  fun tryPresentNextItem(category: FlowItemCategory): Boolean
}