package am5800.harmonie.app.model.flow


interface FlowItemProvider {
  val supportedCategories: Set<FlowItemCategory>
  fun tryPresentNextItem(category: FlowItemCategory): Boolean
}