package am5800.harmonie.app.model.repetition

import org.joda.time.DateTime

enum class AttemptScore {
  Ok, Wrong
}

interface RepetitionService {
  fun submitAttempt(entityId: String, entityCategory: String, score: AttemptScore): DateTime
  fun computeDueDate(entityId: String, entityCategory: String, score: AttemptScore): DateTime
  fun getScheduledEntities(entityCategory: String, dateTime: DateTime): List<String>
  fun getAttemptedItems(entityCategory: String): List<String>
  fun getBinaryScore(entityId: String, entityCategory: String): BinaryLearnScore?
}

