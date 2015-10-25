package am5800.harmonie.model.repetition

import am5800.harmonie.model.Attempt
import am5800.harmonie.model.RepetitionAlgorithm
import am5800.harmonie.model.WordLearnLevel
import org.joda.time.*


public class BucketRepetitionAlgorithm : RepetitionAlgorithm {

    private val buckets = listOf(Hours.ONE.toPeriod(), Days.ONE.toPeriod(), Weeks.TWO.toPeriod(), Months.TWO.toPeriod(), Months.SIX.toPeriod())

    override fun computeLevel(attempts: List<Attempt>): WordLearnLevel {
        val bucket = compute(attempts.sortBy {it.date}).first

        val level =
                if (bucket == -1) WordLearnLevel.NotStarted
                else if (bucket == 0) WordLearnLevel.JustStarted
                else if (bucket == 1) WordLearnLevel.BarelyKnown
                else if (bucket == 2) WordLearnLevel.Confident
                else WordLearnLevel.Known

        return level
    }

    override fun getNextDueDate(attempts: List<Attempt>): DateTime {
        if (attempts.isEmpty()) throw Exception("No attempts")

        val sortedAttempts = attempts.sortBy {it.date}
        var newBucket = compute(sortedAttempts)
        return newBucket.second
    }


    private fun compute(sortedAttempts : List<Attempt>) : Pair<Int, DateTime> {
        if (sortedAttempts.isEmpty()) return Pair(-1, DateTime());
        var bucket = if (isSuccessful(sortedAttempts.first())) 2 else 0
        var prev : Attempt? = null
        var base = sortedAttempts.first()
        for (attempt in sortedAttempts) {
            if (!isSuccessful(attempt)) bucket = 0
            else if (prev != null) {
                if (isAfterDueDate(attempt, base, bucket) && bucket < buckets.size()) {
                    ++bucket
                    base = attempt
                }
            }

            prev = attempt
        }
        val delta = buckets[bucket]
        val lastAttempt = sortedAttempts.last()

        if (!isSuccessful(lastAttempt)) base = lastAttempt
        return Pair(bucket, base.date.plus(delta))
    }

    private fun isSuccessful(attempt: Attempt) = attempt.score == 1.0f && attempt.success

    private fun isAfterDueDate(currentAttempt : Attempt, previousAttempt : Attempt, bucket : Int) : Boolean {
        return previousAttempt.date + buckets[bucket] <= currentAttempt.date
    }
}