package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.onChange
import am5800.harmonie.app.model.flow.FlowManager
import org.joda.time.format.PeriodFormatterBuilder

class DefaultFlowControllerOwnerViewModel(private val flowManager: FlowManager,
                                          lifetime: Lifetime) : ViewModelBase(lifetime) {
  val timeString = Property(lifetime, "")
  val statusVisibility = Property(lifetime, true)
  val onDueLeft = Property(lifetime, "0")

  init {
    flowManager.currentFlow.forEachValue(lifetime, { flow, lt ->
      if (flow == null) return@forEachValue
      flow.onDueLeft.onChange(lt, onDueLeft, { it.toString() })
      flow.spentTime.onChange(lt, timeString, { duration ->
        val formatter = PeriodFormatterBuilder()
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendMinutes()
            .appendSeparator(":")
            .minimumPrintedDigits(2)
            .appendSeconds()
            .toFormatter()
        formatter.print(duration.toPeriod())
      })
    })
  }

  fun stop() {
    flowManager.stop()
  }
}