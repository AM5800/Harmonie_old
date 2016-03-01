package am5800.harmonie.model

import am5800.harmonie.model.logging.LoggerProvider
import org.joda.time.Duration
import org.joda.time.Seconds
import utils.Lifetime
import utils.Property
import utils.SequentialLifetime
import utils.Signal
import java.util.*
import kotlin.concurrent.schedule


class FlowManager(private val lifetime: Lifetime, private val loggerProvider: LoggerProvider) {
  private val providersQueue = LinkedList<FlowItemProvider>()
  private val lifetimes = SequentialLifetime(lifetime)
  private val logger = loggerProvider.getLogger(javaClass)

  private var currentSettings: FlowSettings? = null
  val isEmptySignal = Signal<Unit>(lifetime)

  fun start(providers: List<FlowItemProvider>, settings: FlowSettings, duration: Duration?) {
    logger.info("Flow started")
    if (duration == null) timeLeft.value = null
    else {
      timeLeft.value = duration

      val timerLifetime = lifetimes.next()
      val timer = Timer(true)
      timerLifetime.addAction { timer.cancel() }

      timer.schedule(0, 1000, {
        val value = timeLeft.value
        if (value == Duration.ZERO || value == null) {
          timer.cancel()
          return@schedule
        }
        timeLeft.value = value.minus(Seconds.ONE.toStandardDuration())
      })
    }

    providersQueue.clear()
    providersQueue.addAll(providers)

    currentSettings = settings
    next()
  }

  fun next() {
    if (timeLeft.value == Duration.ZERO) {
      finishFlow()
      return
    }

    val settings = currentSettings!!

    val provider = providersQueue.firstOrNull { it.tryPresentNextItem(settings) }
    if (provider == null) {
      finishFlow()
      return
    }

    providersQueue.remove(provider)
    providersQueue.addLast(provider)
  }

  private fun finishFlow() {
    providersQueue.clear()
    currentSettings = null
    isEmptySignal.fire(Unit)
  }

  val timeLeft = Property<Duration?>(lifetime, null)
}