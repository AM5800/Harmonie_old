package am5800.harmonie.android.model.dbAccess

import am5800.harmonie.app.model.dbAccess.AttemptScore
import am5800.harmonie.app.model.dbAccess.AttemptsService
import org.joda.time.DateTime

class AttemptsServiceImpl() : AttemptsService {
  override fun submitAttempt(entityId: String, entityCategory: String, score: AttemptScore) {

  }

  override fun computeDueDate(entityId: String, entityCategory: String, score: AttemptScore): DateTime {
    return DateTime.now()
  }

  override fun getScheduledEntities(entityCategory: String, dateTime: DateTime): List<String> {
    return emptyList()
  }

}