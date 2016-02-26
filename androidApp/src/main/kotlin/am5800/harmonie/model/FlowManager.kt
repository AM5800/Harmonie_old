package am5800.harmonie.model

import Lifetime
import SequentialLifetime
import Signal
import am5800.harmonie.model.logging.LoggerProvider
import org.joda.time.Duration
import java.util.*


class FlowScore(val right: Int, val total: Int)

class FlowItemResult(
        val success: Boolean,
        val increaseRight: Boolean,
        val increaseTotal: Boolean,
        val reschedule: Boolean,
        val score: Float,
        val note: String?)

class FlowManager(private val loggerProvider: LoggerProvider,
                  lifetime: Lifetime,
                  private val historyManager: AttemptsHistoryManager,
                  private val scheduler: EntityScheduler,
                  private val repetitionAlg: RepetitionAlgorithm,
                  private val newEntitiesSource: NewEntitiesSource) {
  val started: Signal<Flow> = Signal(lifetime)
  private val sequentialLifetime = SequentialLifetime(lifetime)
  var currentFlow: Flow? = null
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