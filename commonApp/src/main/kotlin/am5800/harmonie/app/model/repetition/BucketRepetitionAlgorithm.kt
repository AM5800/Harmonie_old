package am5800.harmonie.app.model.repetition

import org.joda.time.*


class BucketRepetitionAlgorithm() : RepetitionAlgorithm {
  override fun getBinaryScore(attempts: List<Attempt>): LearnScore? {
    if (attempts.isEmpty()) return null
    val sortedAndFiltered = attempts.sortedBy { it.dateTime }.takeLast(10)
    val score = sortedAndFiltered.count { isSuccessful(it) } / sortedAndFiltered.count().toDouble()
    if (score >= 0.5) return LearnScore.Good
    return LearnScore.Bad
  }

  val buckets: List<Period> = listOf(Minutes.TWO.toPeriod(), Days.ONE.toPeriod(), Weeks.TWO.toPeriod(), Months.TWO.toPeriod(), Months.SIX.toPeriod())

  override fun getNextDueDate(attempts: List<Attempt>): DateTime {
    if (attempts.isEmpty()) throw Exception("No attempts")

    val sortedAttempts = attempts.sortedBy { it.dateTime }
    val newBucket = compute(sortedAttempts)
    return newBucket.second
  }


  private fun compute(sortedAttempts: List<Attempt>): Pair<Int, DateTime> {
    if (sortedAttempts.isEmpty()) return Pair(-1, DateTime())
    var bucket = if (isSuccessful(sortedAttempts.first())) 2 else 0
    var base = sortedAttempts.first()
    for (attempt in sortedAttempts.drop(1)) {
      if (!isSuccessful(attempt)) {
        bucket = 0
        base = attempt
      } else {
        if (isAfterDueDate(attempt, base, bucket)) {
          ++bucket
          base = attempt
        }
      }
    }
    val delta = if (bucket < buckets.size) buckets[bucket] else buckets.last()
    val lastAttempt = sortedAttempts.last()

    if (!isSuccessful(lastAttempt)) base = lastAttempt
    return Pair(bucket, base.dateTime.plus(delta))
  }

  private fun isSuccessful(attempt: Attempt) = attempt.score == LearnScore.Good

  private fun isAfterDueDate(currentAttempt: Attempt, previousAttempt: Attempt, bucket: Int): Boolean {
    return previousAttempt.dateTime + buckets[bucket] <= currentAttempt.dateTime
  }
}