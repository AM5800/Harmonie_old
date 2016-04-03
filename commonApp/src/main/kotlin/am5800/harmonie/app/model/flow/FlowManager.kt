package am5800.harmonie.app.model.flow

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.SequentialLifetime
import am5800.common.utils.Signal
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.getRandom
import am5800.harmonie.app.model.logging.LoggerProvider
import org.joda.time.Duration
import org.joda.time.Seconds
import java.util.*
import kotlin.concurrent.schedule


class FlowManager(private val lifetime: Lifetime,
                  private val loggerProvider: LoggerProvider,
                  private val providers: Collection<FlowItemProvider>,
                  debugOptions: DebugOptions) {
  private val lifetimes = SequentialLifetime(lifetime)
  private val logger = loggerProvider.getLogger(javaClass)
  private val random = debugOptions.getRandom()

  private var currentDistribution: CategoryDistribution? = null
  val isEmptySignal = Signal<Unit>(lifetime)

  fun start(distribution: CategoryDistribution, duration: org.joda.time.Duration?) {
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

    currentDistribution = distribution
    next()
  }

  fun next() {
    if (timeLeft.value == Duration.ZERO) {
      finishFlow()
      return
    }

    val category = currentDistribution!!.getCategory(random.nextDouble())

    val provider = providers
        .filter { it.supportedCategories.contains(category) }
        .firstOrNull { it.tryPresentNextItem(category) }
    if (provider == null) {
      finishFlow()
      return
    }
  }

  private fun finishFlow() {
    currentDistribution = null
    isEmptySignal.fire(Unit)
  }

  val timeLeft = Property<Duration>(lifetime, null)
}