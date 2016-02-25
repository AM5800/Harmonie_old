import am5800.harmonie.model.Attempt
import am5800.harmonie.model.GermanWordId
import am5800.harmonie.model.WordLearnLevel
import am5800.harmonie.model.repetition.BucketRepetitionAlgorithm
import am5800.harmonie.model.words.PartOfSpeech
import org.joda.time.DateTime
import org.joda.time.Period
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class BucketAlgTests {
  private val now = DateTime(2015, 1, 1, 0, 0, 0, 0)
  private val alg = BucketRepetitionAlgorithm()
  private val id = GermanWordId("word", PartOfSpeech.Noun, null)

  @Test fun testKnown() {
    val attempts = listOf(mkAttempt(true))
    val level = alg.computeLevel(attempts)
    assertEquals(WordLearnLevel.Confident, level)
  }

  @Test fun secondBeforeDueDate() {
    val attempts = listOf(mkAttempt(false), mkAttempt(true, alg.buckets[0].minusMinutes(2)))
    val dueDate = alg.getNextDueDate(attempts)
    assertEquals(now.plus(alg.buckets[0]), dueDate)
  }

  @Test fun secondBeforeDueDateWrong() {
    val secondAttempt = mkAttempt(false, alg.buckets[0].minusMinutes(2))
    val attempts = listOf(mkAttempt(false), secondAttempt)
    val dueDate = alg.getNextDueDate(attempts)
    assertEquals(secondAttempt.date.plus(alg.buckets[0]), dueDate)
  }

  @Test fun secondAfterDueDate() {
    val secondAttempt = mkAttempt(true, alg.buckets[0].plusMinutes(10))
    val attempts = listOf(mkAttempt(false), secondAttempt)
    val dueDate = alg.getNextDueDate(attempts)
    assertEquals(secondAttempt.date.plus(alg.buckets[1]), dueDate)
  }

  @Test fun secondAfterDueDateWrong() {
    val secondAttempt = mkAttempt(false, alg.buckets[0].plusMinutes(10))
    val attempts = listOf(mkAttempt(false), secondAttempt)
    val dueDate = alg.getNextDueDate(attempts)
    assertEquals(secondAttempt.date.plus(alg.buckets[0]), dueDate)
  }


  @Test fun testAlmostIdealSequence() {
    val attempts = mkAlmostIdealSequence()
    val level = alg.computeLevel(attempts)
    assertEquals(WordLearnLevel.Known, level)
  }


  @Test fun testAlmostIdealSequenceWrong() {
    val sequence = mkAlmostIdealSequence().take(4)
    val attempts = sequence.plus(mkAttempt(false, sequence.last().date))
    val level = alg.computeLevel(attempts)
    val dueDate = alg.getNextDueDate(attempts)
    assertEquals(WordLearnLevel.JustStarted, level)
    assertEquals(attempts.last().date.plus(alg.buckets[0]), dueDate)
  }

  @Test fun testAlmostIdealSequenceWrongRight() {
    val sequence = mkAlmostIdealSequence().take(4)
    val attempt1 = mkAttempt(false, sequence.last().date.plusMinutes(5))
    val attempt2 = mkAttempt(true, attempt1.date.plusMinutes(5))
    val attempts = sequence.plus(attempt1).plus(attempt2)
    val level = alg.computeLevel(attempts)
    val dueDate = alg.getNextDueDate(attempts)
    assertEquals(WordLearnLevel.JustStarted, level)
    assertEquals(attempt1.date.plus(alg.buckets[0]), dueDate)
  }

  private fun mkAlmostIdealSequence(): List<Attempt> {
    val attempts = listOf(mkAttempt(false)).plus(alg.buckets.fold(ArrayList<Attempt>(), { acc, period ->
      if (acc.isEmpty()) {
        acc.add(mkAttempt(true, period.plusMinutes(5)))
      } else {
        acc.add(mkAttempt(true, acc.last().date.plus(period.plusMinutes(5))))
      }
      acc
    }))
    return attempts
  }


  private fun mkAttempt(success: Boolean, period: Period = Period.ZERO) = Attempt(id, now.plus(period), 1.0f, null, success)
  private fun mkAttempt(success: Boolean, date: DateTime) = Attempt(id, date, 1.0f, null, success)
}