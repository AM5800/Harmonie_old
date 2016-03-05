package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.flow.FlowManager
import org.joda.time.Duration

class DefaultFlowControllerOwnerViewModel(private val flowManager: FlowManager, lifetime: Lifetime) {
  val timeLeft = Property<Duration>(lifetime, null)
  val statusVisibility = Property<Visibility>(lifetime, Visibility.Visible)

  init {
    flowManager.timeLeft.bind(lifetime, { args ->
      val timeLeft = args.newValue
      if (timeLeft == null) {
        statusVisibility.value = Visibility.Collapsed
      } else {
        statusVisibility.value = Visibility.Visible
        this.timeLeft.value = timeLeft
      }
    })
  }
}