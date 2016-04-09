package am5800.harmonie.app.model.features.flow


interface FlowItemProvider {
  val supportedCategories: Set<FlowItemCategory>
  fun tryPresentNextItem(category: FlowItemCategory): Boolean
}