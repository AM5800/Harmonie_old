package model.features

import am5800.harmonie.app.model.repetition.Attempt
import am5800.harmonie.app.model.repetition.BucketRepetitionAlgorithm
import am5800.harmonie.app.model.repetition.LearnScore
import org.joda.time.*
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
    Assert.assertEquals(LearnScore.Bad, score)
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
    Assert.assertEquals(LearnScore.Good, score)
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
    Assert.assertEquals(LearnScore.Bad, score)
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
    Assert.assertEquals(LearnScore.Good, score)
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
      ok(Seconds.ONE)
      ok(Seconds.ONE)
      ok(Seconds.ONE)
      ok(Minutes.ONE)
      ok(Seconds.ONE)
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

    fun start(score: LearnScore) {
      if (result.isNotEmpty()) throw Exception("Start should only be called on empty builder")
      result.add(Attempt(score, DateTime.now()))
    }

    fun next(period: ReadablePeriod, score: LearnScore) {
      if (result.isEmpty()) throw Exception("Start was not called!")
      val last = result.last()
      result.add(Attempt(score, last.dateTime.plus(period)))
    }

    fun wrong() = start(LearnScore.Bad)
    fun wrong(period: ReadablePeriod) = next(period, LearnScore.Bad)
    fun ok() = start(LearnScore.Good)
    fun ok(period: ReadablePeriod) = next(period, LearnScore.Good)
  }
}