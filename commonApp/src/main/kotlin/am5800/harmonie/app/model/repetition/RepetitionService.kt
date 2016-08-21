package am5800.harmonie.app.model.repetition

import org.joda.time.DateTime

enum class LearnScore {
  Good, Bad
}

interface RepetitionService {
  fun submitAttempt(entityId: String, entityCategory: String, score: LearnScore): DateTime
  fun getAttemptedItems(entityCategory: String): List<String>
  fun getDueDates(entityIds: List<String>, entityCategory: String): Map<String, DateTime>
  fun countOnDueItems(entityCategory: String, dateTime: DateTime): Int
  fun getNextScheduledEntity(entityCategory: String, dateTime: DateTime): String?
  fun remove(entityId: String, entityCategory: String)
}

