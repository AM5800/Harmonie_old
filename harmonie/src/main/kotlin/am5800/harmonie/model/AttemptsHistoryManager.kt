package am5800.harmonie.model

public interface AttemptsHistoryManager {
    fun getAttempts(entity: EntityId): List<Attempt>
    fun addAttempt(attempt: Attempt)
    fun remove(items: List<EntityId>)
    fun getKeys(): List<EntityId>
}