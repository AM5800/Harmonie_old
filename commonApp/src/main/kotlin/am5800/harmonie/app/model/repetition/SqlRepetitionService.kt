package am5800.harmonie.app.model.repetition

import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.sql.UserDb
import am5800.harmonie.app.model.sql.query1
import am5800.harmonie.app.model.sql.query2
import org.joda.time.DateTime

class SqlRepetitionService(private val repetitionAlgorithm: RepetitionAlgorithm,
                           private val db: UserDb,
                           debugOptions: DebugOptions) : RepetitionService {
  override fun getDueDates(entityIds: List<String>, entityCategory: String): Map<String, DateTime> {
    val ids = entityIds.map { "'$it'" }.joinToString(", ")
    val query = """
      SELECT entityId, dueDate FROM dueDatesCache WHERE entityId IN ($ids)
    """

    return db.query2<String, Long>(query).map { Pair(it.first, DateTime(it.second)) }.toMap()
  }

  override fun remove(entityId: String, entityCategory: String) {
    db.execute("DELETE FROM attempts WHERE entityId='$entityId' AND entityCategory='$entityCategory'")
  }

  override fun getBinaryScore(entityId: String, entityCategory: String): LearnScore? {
    return repetitionAlgorithm.getBinaryScore(getAttempts(entityCategory, entityId))
  }

  override fun getAttemptedItems(entityCategory: String): List<String> {
    return db.query1("SELECT DISTINCT(entityId) FROM attempts WHERE entityCategory='$entityCategory'")
  }

  init {
    if (debugOptions.dropAttemptsOnStart) db.execute("DROP TABLE IF EXISTS attempts")
    db.execute("CREATE TABLE IF NOT EXISTS attempts (entityId STRING, entityCategory STRING, dateTime INTEGER, score TEXT)")
    db.execute("CREATE TABLE IF NOT EXISTS dueDatesCache (entityId STRING, entityCategory STRING, dueDate INTEGER, PRIMARY KEY(entityId, entityCategory))")
  }

  override fun submitAttempt(entityId: String, entityCategory: String, score: LearnScore): DateTime {
    db.execute("INSERT INTO attempts VALUES('$entityId', '$entityCategory', ${DateTime.now().millis}, '${score.toString()}')")
    val attempts = getAttempts(entityCategory, entityId)
    val dueDate = repetitionAlgorithm.getNextDueDate(attempts.plus(Attempt(score, DateTime.now())))
    db.execute("INSERT OR REPLACE INTO dueDatesCache VALUES('$entityId', '$entityCategory', ${dueDate.millis})")
    return dueDate
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

    val query = """
      SELECT entityId FROM dueDatesCache WHERE entityCategory='$entityCategory' AND dueDate <= ${dateTime.millis}
    """

    return db.query1<String>(query)
  }
}