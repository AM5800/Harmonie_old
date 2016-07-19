package am5800.harmonie.app.model.services.flow

interface FlowItemProvider {
  val supportedTags: Set<FlowItemTag>
  fun getAvailableDataSetSize(tag: FlowItemTag): Int
  fun tryPresentNextItem(tag: FlowItemTag): Boolean
}