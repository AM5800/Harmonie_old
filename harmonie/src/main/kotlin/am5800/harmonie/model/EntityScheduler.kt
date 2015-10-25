package am5800.harmonie.model

import org.joda.time.DateTime

public data class EntitySchedule(val entity: EntityId, val dueDate: DateTime)

public interface EntityScheduler {
    fun getAllScheduledItems(): List<EntitySchedule>
    fun scheduleItem(entity: EntityId, dueDate: DateTime)
    fun remove(items: List<EntityId>)
}