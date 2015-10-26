package am5800.harmonie.model


public interface EntityManager {
    public fun getEntitiesForText(textPartId: TextPartId) : List<EntityId>
    public fun getExamples(entityId: EntityId) : List<Example>
}