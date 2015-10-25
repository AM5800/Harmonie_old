import am5800.harmonie.model.Attempt
import am5800.harmonie.model.EntityIds.EntityId
import model.WordLearnLevel
import common.repetition.BucketRepetitionAlgoritm
import org.joda.time.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class BucketAlgTests {
    private val now = DateTime(2015, 1, 1, 0, 0, 0, 0)
    private val alg = BucketRepetitionAlgoritm()
    private val id = am5800.harmonie.model.EntityIds.EntityId("")

    @Test
    public fun TestKnown() {
        val attempts = listOf(am5800.harmonie.model.Attempt(id, now, 1.0f, null, true))
        val dueDate = alg.getNextDueDate(attempts)
        assertEquals(2, Weeks.weeksBetween(now, dueDate).getWeeks())
    }

    @Test
    public fun HalfHour() {
        val attempts = listOf(am5800.harmonie.model.Attempt(id, now, 1.0f, null, false), am5800.harmonie.model.Attempt(id, now.plus(Minutes.minutes(30)), 1.0f, null, true))
        val dueDate = alg.getNextDueDate(attempts)
        assertEquals(now.plus(Hours.ONE), dueDate)
    }

    @Test
    public fun HalfHourWrong() {
        val attempts = listOf(am5800.harmonie.model.Attempt(id, now, 1.0f, null, true), am5800.harmonie.model.Attempt(id, now.plus(Minutes.minutes(30)), 0.0f, null, true))
        val dueDate = alg.getNextDueDate(attempts)
        assertEquals(now.plus(Hours.ONE).plus(Minutes.minutes(30)), dueDate)
    }

    @Test
    public fun MoreThanOneHour() {
        val attempts = listOf(am5800.harmonie.model.Attempt(id, now, 1.0f, null, false), am5800.harmonie.model.Attempt(id, now.plus(Minutes.minutes(90)), 1.0f, null, true))
        val dueDate = alg.getNextDueDate(attempts)
        assertEquals(now.plus(Days.ONE).plus(Minutes.minutes(90)), dueDate)
    }

    @Test
    public fun MoreThanOneHourWrong() {
        val lastAttemptDate = now.plus(Minutes.minutes(90))
        val attempts = listOf(am5800.harmonie.model.Attempt(id, now, 1.0f, null, true), am5800.harmonie.model.Attempt(id, lastAttemptDate, 0.0f, null, false))
        val dueDate = alg.getNextDueDate(attempts)
        assertEquals(lastAttemptDate.plus(Hours.ONE), dueDate)
    }


    @Test
    public fun TestWithLongBreak() {
        val lastAttemptDate = now.plus(Days.TWO)
        val attempts = listOf(am5800.harmonie.model.Attempt(id, now, 1.0f, null, false), am5800.harmonie.model.Attempt(id, lastAttemptDate, 1.0f, null, true))
        val dueDate = alg.getNextDueDate(attempts)
        assertEquals(lastAttemptDate.plus(Days.ONE), dueDate)
    }

    @Test
    public fun TestWithLongBreakWrong() {
        val lastAttemptDate = now.plus(Days.TWO)
        val attempts = listOf(am5800.harmonie.model.Attempt(id, now, 1.0f, null, true), am5800.harmonie.model.Attempt(id, lastAttemptDate, 0.0f, null, true))
        val dueDate = alg.getNextDueDate(attempts)
        assertEquals(lastAttemptDate.plus(Hours.ONE), dueDate)
    }

    @Test
    public fun TestALot() {
        val intervals = listOf(Minutes.minutes(90), Hours.hours(22), Hours.hours(5))
        val dates = intervals.fold(arrayListOf(now), { dates, interval ->
            dates.add(dates.last() + interval)
            dates
        })
        val attempts = dates.map { am5800.harmonie.model.Attempt(id, it, 1.0f, null, true) }
        assertEquals(WordLearnLevel.Confident, alg.computeWordLearnLevel(attempts))
    }
}