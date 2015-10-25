package am5800.harmonie.model

public interface FlowItemsSource {
    fun getItems(amount : Int, deprecatedItems : Set<EntityId>) : List<EntityId>
}