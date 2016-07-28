package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.feedback.ErrorReportingService
import am5800.harmonie.app.model.localization.LocalizationService

class IssueReportingMenuHelper {
  companion object {
    fun createMenuItems(reportingService: ErrorReportingService, localizationService: LocalizationService, lifetime: Lifetime, describeState: () -> String): List<SimpleCommand> {
      val title1 = localizationService.createProperty(lifetime, { it.reportUnclearSentencePair })
      val item1 = SimpleCommand(title1, { reportingService.report("UnclearSentencePair", describeState()) })

      val title2 = localizationService.createProperty(lifetime, { it.reportWrongTranslation })
      val item2 = SimpleCommand(title2, { reportingService.report("WrongTranslation", describeState()) })

      val title3 = localizationService.createProperty(lifetime, { it.reportOther })
      val item3 = SimpleCommand(title3, { reportingService.report("Other", describeState()) })

      return listOf(item1, item2, item3)
    }
  }
}