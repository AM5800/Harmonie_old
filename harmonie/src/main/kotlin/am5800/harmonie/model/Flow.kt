package am5800.harmonie.model

import am5800.harmonie.model.logging.LoggerProvider
import am5800.harmonie.model.util.Property
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Seconds
import java.util.*
import kotlin.concurrent.schedule

public class Flow(
        public val flowType: EnumSet<FlowType>,
        public val lifetime : Lifetime,
        private val itemsSource : FlowItemsSource,
        private val continuation: (() -> Unit)?,
        loggerProvider : LoggerProvider,
        time: Duration?,
        private val historyManager : AttemptsHistoryManager,
        private val scheduler : EntityScheduler,
        private val repetitionAlg : RepetitionAlgorithm) {
    val currentItem: EntityId?
        get() = shortQueue.firstOrNull()
    private val longQueue = LinkedList<EntityId>()
    private val shortQueue = LinkedList<EntityId>()
    private val shortQueueTargetN = 3
    public val timeLeft: Property<Duration> = Property(time ?: Duration.ZERO)
    private val allAttemptedItems = LinkedHashSet<EntityId>()
    public val score: Property<FlowScore> = Property(FlowScore(0, 0))
    private val logger = loggerProvider.getLogger(this.javaClass)

    private fun fillShortQueue() {
        if (longQueue.isEmpty()) {
            val list = itemsSource.getItems(shortQueueTargetN, shortQueue.plus(allAttemptedItems).toSet())
            longQueue.addAll(list)
            if (longQueue.isEmpty() && flowType.contains(FlowType.Unlimited)) {
                // Starting new loop
                allAttemptedItems.clear()
                longQueue.addAll(itemsSource.getItems(shortQueueTargetN, shortQueue.toSet()))
            }
        }

        while (shortQueue.count() < shortQueueTargetN && longQueue.any()) {
            shortQueue.add(longQueue.removeFirst())
        }
    }

    fun next(answer: FlowItemResult): DateTime? {
        val nextDueDate = handleAnswer(answer, shortQueue.firstOrNull())
        if (flowType.contains(FlowType.TimeTrial) && timeLeft.value!! == Duration.ZERO) {
            logger.info("Time is over. Executing continuation")
            shortQueue.clear()
            longQueue.clear()
            continuation?.invoke()
            return nextDueDate
        }
        if (!answer.reschedule) {
            if (!shortQueue.isEmpty()) shortQueue.removeFirst()
            fillShortQueue()
        } else if (!shortQueue.isEmpty()) {
            shortQueue.addLast(shortQueue.removeFirst())
        }
        if (shortQueue.isEmpty()) {
            logger.info("Flow finished. Executing continuation")
            continuation?.invoke()
        }

        return nextDueDate
    }

    private fun handleAnswer(answer: FlowItemResult, entityId: EntityId?): DateTime? {
        updateScore(answer)
        if (entityId == null) return null
        allAttemptedItems.add(entityId)

        historyManager.addAttempt(Attempt(entityId, DateTime(), answer.score, answer.note, answer.success))
        val attempts = historyManager.getAttempts(entityId)

        val nextDueDate = repetitionAlg.getNextDueDate(attempts)
        scheduler.scheduleItem(entityId, nextDueDate)

        return nextDueDate
    }

    private fun updateScore(answer: FlowItemResult) {
        val score = score.value!!
        var right = score.right
        var total = score.total
        if (answer.increaseRight) right++
        if (answer.increaseTotal) total++
        this.score.value = FlowScore(right, total)
    }

    init {
        if (flowType.contains(FlowType.TimeTrial) && time != null) {
            startTimer(lifetime)
        }
        fillShortQueue()
        logger.info("Flow started. type: $flowType")
        logger.info("${shortQueue.count()} items in short queue. ${longQueue.count()} items in long queue")
    }

    private fun startTimer(lifetime: Lifetime) {
        val timer = Timer()
        timer.schedule(0, 1000, {
            val value = timeLeft.value
            if (value == Duration.ZERO || value == null) {
                timer.cancel()
                return@schedule
            }
            timeLeft.value = value.minus(Seconds.ONE.toStandardDuration())
        })
        lifetime.addAction { timer.cancel () }
    }
}