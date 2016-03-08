package am5800.harmonie.app.model.dbAccess

import org.joda.time.DateTime

enum class AttemptScore {
  Ok, Wrong
}

interface AttemptsService {
  fun submitAttempt(entityId: String, entityCategory: String, score: AttemptScore)
  fun computeDueDate(entityId: String, entityCategory: String, score: AttemptScore): DateTime
  fun getScheduledEntities(entityCategory: String, dateTime: DateTime): List<String>
}