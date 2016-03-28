package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.flow.FlowManager
import org.joda.time.Duration

class DefaultFlowControllerOwnerViewModel(private val flowManager: FlowManager, lifetime: Lifetime) {
  val timeLeft = Property<Duration>(lifetime, null)
  val statusVisibility = Property(lifetime, true)

  init {
    flowManager.timeLeft.bind(lifetime, { args ->
      val timeLeft = args.newValue
      if (timeLeft == null) {
        statusVisibility.value = false
      } else {
        statusVisibility.value = true
        this.timeLeft.value = timeLeft
      }
    })
  }
}