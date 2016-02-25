package am5800.harmonie.model

interface FlowItemsSource {
  fun getItems(amount: Int, deprecatedItems: Set<EntityId>): List<EntityId>
}