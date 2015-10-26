package am5800.harmonie.model

import am5800.harmonie.model.logging.LoggerProvider
import am5800.harmonie.model.util.SequentialLifetime
import am5800.harmonie.model.util.Signal
import org.joda.time.Duration
import java.util.*


public class FlowScore(public val right: Int, public val total: Int)

public class FlowItemResult(
        public val success : Boolean,
        public val increaseRight: Boolean,
        public val increaseTotal: Boolean,
        public val reschedule: Boolean,
        public val score: Float,
        public val note: String?)

public class FlowManager(private val loggerProvider: LoggerProvider,
                         lifetime: Lifetime,
                         private val historyManager: AttemptsHistoryManager,
                         private val scheduler : EntityScheduler,
                         private val repetitionAlg : RepetitionAlgorithm,
                         private val newEntitiesSource : NewEntitiesSource) {
    public val started: Signal<Flow> = Signal(lifetime)
    private val sequentialLifetime = SequentialLifetime(lifetime)
    public var currentFlow: Flow? = null
        private set

    fun start(time: Duration, continuation: (() -> Unit)? = null): Boolean {
        val lt = sequentialLifetime.next() ?: return false
        val source = InfiniteFlowItemsSource(scheduler, newEntitiesSource)
        val flow = Flow(EnumSet.of (FlowType.TimeTrial, FlowType.Unlimited), lt, source, continuation, loggerProvider, time, historyManager, scheduler, repetitionAlg)
        currentFlow = flow
        started.fire(flow)
        return true
    }
}