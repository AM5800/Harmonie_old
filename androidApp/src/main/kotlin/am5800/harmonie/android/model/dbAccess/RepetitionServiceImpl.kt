package am5800.harmonie.android.model.dbAccess

import am5800.harmonie.android.DebugOptions
import am5800.harmonie.app.model.Attempt
import am5800.harmonie.app.model.RepetitionAlgorithm
import am5800.harmonie.app.model.dbAccess.AttemptScore
import am5800.harmonie.app.model.dbAccess.RepetitionService
import org.joda.time.DateTime

class RepetitionServiceImpl(private val repetitionAlgorithm: RepetitionAlgorithm,
                            private val db: PermanentDb,
                            private val debugOptions: DebugOptions) : RepetitionService {
  init {
    db.execute("CREATE TABLE IF NOT EXISTS attempts (entityId STRING, entityCategory STRING, dateTime INTEGER, score REAL)")
  }

  override fun submitAttempt(entityId: String, entityCategory: String, score: AttemptScore): DateTime {
    val dueDate = computeDueDate(entityId, entityCategory, score)
    val dateTime = DateTime.now().millis
    if (!debugOptions.readonlyDatabase)
      db.execute("INSERT INTO attempts VALUES($entityId, $entityCategory, $dateTime, $score)")
    return dueDate
  }

  override fun computeDueDate(entityId: String, entityCategory: String, score: AttemptScore): DateTime {
    val query = "SELECT dateTime, score FROM attempts WHERE entityId='$entityId' AND entityCategory='$entityCategory'"
    val attempts = db.query2<Long, Double>(query).map { Attempt(it.second, DateTime(it.first)) }
    return repetitionAlgorithm.getNextDueDate(attempts.plus(Attempt(convertScore(score), DateTime.now())))
  }

  private fun convertScore(score: AttemptScore): Double {
    return when (score) {
      AttemptScore.Ok -> 1.0
      else -> 0.0
    }
  }

  override fun getScheduledEntities(entityCategory: String, dateTime: DateTime): List<String> {
    val query = "SELECT entityId, dateTime, score FROM attempts WHERE entityCategory='$entityCategory'"

    // TODO: such approach is probably slow
    val dueDates = db.query3<String, Long, Double>(query)
        .groupBy { it.value1 }
        .mapValues { pair -> pair.value.map { Attempt(it.value3, DateTime(it.value2)) } }
        .mapValues { repetitionAlgorithm.getNextDueDate(it.value) }

    return dueDates.filter { it.value < DateTime.now() }.map { it.key }
  }
}