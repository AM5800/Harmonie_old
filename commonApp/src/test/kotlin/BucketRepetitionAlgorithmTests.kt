import am5800.harmonie.app.model.repetition.Attempt
import am5800.harmonie.app.model.repetition.AttemptScore
import am5800.harmonie.app.model.repetition.BinaryLearnScore
import am5800.harmonie.app.model.repetition.BucketRepetitionAlgorithm
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Minutes
import org.joda.time.ReadablePeriod
import org.junit.Assert
import org.junit.Test

class BucketRepetitionAlgorithmTests {
  private val alg = BucketRepetitionAlgorithm()

  @Test
  fun testBinaryScoreAllWrong() {
    val attempts = buildAttempts {
      wrong()
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
    }

    val score = alg.getBinaryScore(attempts)
    Assert.assertEquals(BinaryLearnScore.Bad, score)
  }

  @Test
  fun testBinaryScore50x50() {
    val attempts = buildAttempts {
      ok()
      wrong(Days.ONE)
      ok(Days.ONE)
      wrong(Days.ONE)
      ok(Days.ONE)
      wrong(Days.ONE)
      ok(Days.ONE)
      wrong(Days.ONE)
      ok(Days.ONE)
      wrong(Days.ONE)
    }

    val score = alg.getBinaryScore(attempts)
    Assert.assertEquals(BinaryLearnScore.Good, score)
  }

  @Test
  fun testBinaryScoreLastGoodButNotEnough() {
    val attempts = buildAttempts {
      wrong()
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      ok(Days.ONE)
      ok(Days.ONE)
      ok(Days.ONE)
      ok(Days.ONE)
    }

    val score = alg.getBinaryScore(attempts)
    Assert.assertEquals(BinaryLearnScore.Bad, score)
  }


  @Test
  fun testBinaryScoreLastGood() {
    val attempts = buildAttempts {
      wrong()
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      wrong(Days.ONE)
      ok(Days.ONE)
      ok(Days.ONE)
      ok(Days.ONE)
      ok(Days.ONE)
      ok(Days.ONE)
    }

    val score = alg.getBinaryScore(attempts)
    Assert.assertEquals(BinaryLearnScore.Good, score)
  }

  @Test
  fun testFirstSuccessfulAttempt() {
    val attempts = buildAttempts { ok() }
    val dueDate = alg.getNextDueDate(attempts)
    val expectedDueDate = attempts.last().dateTime.plus(alg.buckets[2])
    Assert.assertEquals(expectedDueDate, dueDate)
  }

  @Test
  fun testFirstFailedAttempt() {
    val attempts = buildAttempts { wrong() }
    val dueDate = alg.getNextDueDate(attempts)
    val expectedDueDate = attempts.last().dateTime.plus(alg.buckets[0])
    Assert.assertEquals(expectedDueDate, dueDate)
  }

  @Test
  fun testConsecutiveRepetitionsDoNotChangeFirstVerdict() {
    val attempts = buildAttempts {
      wrong()
      ok(Minutes.ONE)
      ok(Minutes.ONE)
      ok(Minutes.ONE)
      ok(Minutes.ONE)
      ok(Minutes.ONE)
    }

    val dueDate = alg.getNextDueDate(attempts)
    val expectedDueDate = attempts.first().dateTime.plus(alg.buckets[0])
    Assert.assertEquals(expectedDueDate, dueDate)
  }

  @Test
  fun testSecondLevelAdvancing() {
    val attempts = buildAttempts {
      wrong()
      ok(alg.buckets[0].plus(Minutes.ONE))
      ok(alg.buckets[1].plus(Minutes.ONE))
    }

    val dueDate = alg.getNextDueDate(attempts)
    val expectedDueDate = attempts.last().dateTime.plus(alg.buckets[2])
    Assert.assertEquals(expectedDueDate, dueDate)
  }

  @Test
  fun testDropFromSecondLevel() {
    val attempts = buildAttempts {
      wrong()
      ok(alg.buckets[0].plus(Minutes.ONE))
      ok(alg.buckets[1].plus(Minutes.ONE))
      wrong(Minutes.ONE)
    }

    val dueDate = alg.getNextDueDate(attempts)
    val expectedDueDate = attempts.last().dateTime.plus(alg.buckets[0])
    Assert.assertEquals(expectedDueDate, dueDate)
  }

  private fun buildAttempts(init: AttemptsBuilder.() -> Unit): List<Attempt> {
    val builder = AttemptsBuilder()
    init(builder)
    return builder.result
  }

  private class AttemptsBuilder {
    val result = mutableListOf<Attempt>()

    fun start(score: AttemptScore) {
      if (result.isNotEmpty()) throw Exception("Start should only be called on empty builder")
      result.add(Attempt(score, DateTime.now()))
    }

    fun next(period: ReadablePeriod, score: AttemptScore) {
      if (result.isEmpty()) throw Exception("Start was not called!")
      val last = result.last()
      result.add(Attempt(score, last.dateTime.plus(period)))
    }

    fun wrong() = start(AttemptScore.Wrong)
    fun wrong(period: ReadablePeriod) = next(period, AttemptScore.Wrong)
    fun ok() = start(AttemptScore.Ok)
    fun ok(period: ReadablePeriod) = next(period, AttemptScore.Ok)
  }
}