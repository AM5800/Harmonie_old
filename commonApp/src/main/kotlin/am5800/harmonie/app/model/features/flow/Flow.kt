package am5800.harmonie.app.model.features.flow

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.Signal
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.services.EnumerableDistribution
import org.joda.time.Duration
import org.joda.time.Seconds
import java.util.*
import kotlin.concurrent.schedule

class Flow(lifetime: Lifetime,
           private val providers: Collection<FlowItemProvider>,
           debugOptions: DebugOptions,
           private val distribution: EnumerableDistribution<FlowItemCategory>) {
  private val random = debugOptions.random
  private var successful = 0
  private var failed = 0

  val spentTime = Property(lifetime, Duration.ZERO)
  val successRate = Property<Double>(lifetime, null)

  fun next(successDelta: Int, failureDelta: Int) {
    updateSuccessRate(failureDelta, successDelta)

    var provider: FlowItemProvider? = null
    for (i in 1..10) {
      val category = distribution.get(random)

      provider = providers
          .filter { it.supportedCategories.contains(category) }
          .firstOrNull { it.tryPresentNextItem(category) }

      if (provider != null) break
    }

    if (provider == null) {
      isEmptySignal.fire(Unit)
      return
    }
  }

  private fun updateSuccessRate(failureDelta: Int, successDelta: Int) {
    successful += successDelta
    failed += failureDelta
    if (successful == 0 && failed == 0) successRate.value = null
    else if (failed == 0) successRate.value = 1.0
    else successRate.value = successful / failed.toDouble()
  }

  val isEmptySignal = Signal<Unit>(lifetime)


  init {
    val timer = Timer(true)
    lifetime.addAction { timer.cancel() }

    timer.schedule(0, 1000, {
      spentTime.value = spentTime.value!!.plus(Seconds.ONE.toStandardDuration())
    })
  }
}