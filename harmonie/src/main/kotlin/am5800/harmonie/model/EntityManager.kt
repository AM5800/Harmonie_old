package am5800.harmonie.model

import am5800.harmonie.model.Attempt
import am5800.harmonie.model.EntityId
import org.joda.time.DateTime


public interface EntityManager {
    public fun getEntitiesForText(textPartId: TextPartId) : List<EntityId>
    public fun getExamples(entityId: EntityId) : List<Example>
}