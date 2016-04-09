package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.features.flow.FlowManager
import am5800.harmonie.app.model.features.localization.LocalizationService
import am5800.harmonie.app.model.features.localization.LocalizationTable
import org.joda.time.Duration

class DefaultFlowControllerOwnerViewModel(private val flowManager: FlowManager, lifetime: Lifetime, localizationService: LocalizationService) {
  val timeLeft = Property<String>(lifetime, null)
  val statusVisibility = Property(lifetime, true)

  init {
    flowManager.timeLeft.onChange(lifetime, { args ->
      val timeLeft = args.newValue
      if (timeLeft == null) {
        statusVisibility.value = false
      } else {
        statusVisibility.value = true
        val table = localizationService.getCurrentTable()
        this.timeLeft.value = formatTimeLeft(timeLeft, table)
      }
    })
  }

  private fun formatTimeLeft(timeLeft: Duration, localizationTable: LocalizationTable): String {
    val minutes = timeLeft.standardMinutes.toInt()
    if (minutes > 0) return localizationTable.minutesLeft.build(minutes)
    else {
      val seconds = timeLeft.standardSeconds.toInt()
      return localizationTable.secondsLeft.build(seconds)
    }
  }
}