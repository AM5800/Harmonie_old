package am5800.harmonie.model


interface EntityManager {
  fun getEntitiesForText(textPartId: TextPartId): List<EntityId>
  fun getExamples(entityId: EntityId): List<Example>
}