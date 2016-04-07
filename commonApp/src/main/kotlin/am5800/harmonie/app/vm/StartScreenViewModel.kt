package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.flow.FlowItemDistributionService
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.localization.LocalizationService
import org.joda.time.Minutes

class StartScreenViewModel(private val flowManager: FlowManager,
                           lifetime: Lifetime,
                           localizationService: LocalizationService,
                           private val distributionService: FlowItemDistributionService, private val welcomeScreenViewModel: WelcomeScreenViewModel) : ViewModelBase(lifetime) {
  private val defaultDuration = Minutes.minutes(10).toStandardDuration()

  val learnAllText = localizationService.createProperty(lifetime, { it.learnAll })
  val chooseLanguagesText = localizationService.createProperty(lifetime, { it.chooseLanguages })

  fun onActivated() {
    welcomeScreenViewModel.activateIfNeeded()
  }

  fun learnAll() {
    flowManager.start(distributionService.getDistribution(), defaultDuration)
  }

  fun chooseLanguages() {
    welcomeScreenViewModel.activationRequested.fire(Unit)
  }
}