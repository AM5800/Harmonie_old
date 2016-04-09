package am5800.harmonie.app.model.services.impl

import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.repetition.Attempt
import am5800.harmonie.app.model.features.repetition.LearnScore
import am5800.harmonie.app.model.features.repetition.RepetitionAlgorithm
import am5800.harmonie.app.model.features.repetition.RepetitionService
import am5800.harmonie.app.model.services.PermanentDb
import am5800.harmonie.app.model.services.query1
import am5800.harmonie.app.model.services.query2
import am5800.harmonie.app.model.services.query3
import org.joda.time.DateTime

class SqlRepetitionService(private val repetitionAlgorithm: RepetitionAlgorithm,
                           private val db: PermanentDb,
                           private val debugOptions: DebugOptions) : RepetitionService {
  override fun getBinaryScore(entityId: String, entityCategory: String): LearnScore? {
    return repetitionAlgorithm.getBinaryScore(getAttempts(entityCategory, entityId))
  }

  override fun getAttemptedItems(entityCategory: String): List<String> {
    return db.query1("SELECT DISTINCT(entityId) FROM attempts WHERE entityCategory='$entityCategory'")
  }

  init {
    if (debugOptions.resetProgressOnLaunch) db.execute("DROP TABLE IF EXISTS attempts")
    db.execute("CREATE TABLE IF NOT EXISTS attempts (entityId STRING, entityCategory STRING, dateTime INTEGER, score TEXT)")
  }

  override fun submitAttempt(entityId: String, entityCategory: String, score: LearnScore): DateTime {
    val dueDate = computeDueDate(entityId, entityCategory, score)
    val dateTime = DateTime.now().millis
    db.execute("INSERT INTO attempts VALUES('$entityId', '$entityCategory', $dateTime, '${score.toString()}')")
    return dueDate
  }

  override fun computeDueDate(entityId: String, entityCategory: String, score: LearnScore): DateTime {
    val attempts = getAttempts(entityCategory, entityId)
    return repetitionAlgorithm.getNextDueDate(attempts.plus(Attempt(score, DateTime.now())))
  }

  private fun getAttempts(entityCategory: String, entityId: String): List<Attempt> {
    val query = "SELECT dateTime, score FROM attempts WHERE entityId='$entityId' AND entityCategory='$entityCategory'"
    try {
      return db.query2<Long, String>(query).map { Attempt(LearnScore.valueOf(it.second), DateTime(it.first)) }
    } catch(e: Exception) {
      throw e
    }
  }

  override fun getScheduledEntities(entityCategory: String, dateTime: DateTime): List<String> {
    val query = "SELECT entityId, dateTime, score FROM attempts WHERE entityCategory='$entityCategory'"

    val dueDates = db.query3<String, Long, String>(query)
        .groupBy { it.value1 }
        .mapValues { pair -> pair.value.map { Attempt(LearnScore.valueOf(it.value3), DateTime(it.value2)) } }
        .mapValues { repetitionAlgorithm.getNextDueDate(it.value) }

    return dueDates.filter { it.value < DateTime.now() }.map { it.key }
  }
}