package am5800.harmonie.app.model.dbAccess.sql

import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.repetition.*
import org.joda.time.DateTime

class SqlRepetitionService(private val repetitionAlgorithm: RepetitionAlgorithm,
                           private val db: PermanentDb,
                           private val debugOptions: DebugOptions) : RepetitionService {
  override fun getBinaryScore(entityId: String, entityCategory: String): BinaryLearnScore? {
    return repetitionAlgorithm.getBinaryScore(getAttempts(entityCategory, entityId))
  }

  override fun getAttemptedItems(entityCategory: String): List<String> {
    return db.query1("SELECT DISTINCT(entityId) FROM attempts WHERE entityCategory='$entityCategory'")
  }

  init {
    if (debugOptions.clearAttemptsOnLaunch) db.execute("DROP TABLE IF EXISTS attempts")
    db.execute("CREATE TABLE IF NOT EXISTS attempts (entityId STRING, entityCategory STRING, dateTime INTEGER, score REAL)")
  }

  override fun submitAttempt(entityId: String, entityCategory: String, score: AttemptScore): DateTime {
    val dueDate = computeDueDate(entityId, entityCategory, score)
    val dateTime = DateTime.now().millis
    if (!debugOptions.readonlyAttempts)
      db.execute("INSERT INTO attempts VALUES('$entityId', '$entityCategory', $dateTime, ${convertScore(score)})")
    return dueDate
  }

  override fun computeDueDate(entityId: String, entityCategory: String, score: AttemptScore): DateTime {
    val attempts = getAttempts(entityCategory, entityId)
    return repetitionAlgorithm.getNextDueDate(attempts.plus(Attempt(convertScore(score), DateTime.now())))
  }

  private fun getAttempts(entityCategory: String, entityId: String): List<Attempt> {
    val query = "SELECT dateTime, score FROM attempts WHERE entityId='$entityId' AND entityCategory='$entityCategory'"
    try {
      return db.query2<Long, Double>(query).map { Attempt(it.second, DateTime(it.first)) }
    }
    catch(e: Exception) {
      throw e
    }
  }

  private fun convertScore(score: AttemptScore): Double {
    return when (score) {
      AttemptScore.Ok -> 1.0
      else -> 0.0
    }
  }

  override fun getScheduledEntities(entityCategory: String, dateTime: DateTime): List<String> {
    val query = "SELECT entityId, dateTime, score FROM attempts WHERE entityCategory='$entityCategory'"

    val dueDates = db.query3<String, Long, Double>(query)
        .groupBy { it.value1 }
        .mapValues { pair -> pair.value.map { Attempt(it.value3, DateTime(it.value2)) } }
        .mapValues { repetitionAlgorithm.getNextDueDate(it.value) }

    return dueDates.filter { it.value < DateTime.now() }.map { it.key }
  }
}