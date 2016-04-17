package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.features.feedback.FeedbackService
import am5800.harmonie.app.model.features.flow.FlowItemDistributionService
import am5800.harmonie.app.model.features.flow.FlowManager
import am5800.harmonie.app.model.features.localization.LocalizationService

class StartScreenViewModel(private val flowManager: FlowManager,
                           lifetime: Lifetime,
                           localizationService: LocalizationService,
                           private val distributionService: FlowItemDistributionService,
                           private val selectLanguageViewModel: SelectLanguageViewModel,
                           private val feedbackService: FeedbackService) : ViewModelBase(lifetime) {
  val learnAllText = localizationService.createProperty(lifetime, { it.learnAll })
  val chooseLanguagesText = localizationService.createProperty(lifetime, { it.chooseLanguages })
  val sendFeedbackText = localizationService.createProperty(lifetime, { it.sendStatistics })

  fun onActivated() {
    selectLanguageViewModel.activateIfNeeded()
  }

  fun learnAll() {
    flowManager.start(distributionService.getDistribution())
  }

  fun sendFeedback() {
    feedbackService.collectAndSendData()
  }

  fun chooseLanguages() {
    selectLanguageViewModel.activationRequested.fire(Unit)
  }
}