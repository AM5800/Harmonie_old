package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.flow.FlowItemDistributionService
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.localization.LocalizationService
import org.joda.time.Minutes

class StartScreenViewModel(private val flowManager: FlowManager,
                           lifetime: Lifetime,
                           localizationService: LocalizationService,
                           private val distributionService: FlowItemDistributionService) : ViewModelBase(lifetime) {
  private val defaultDuration = Minutes.minutes(10).toStandardDuration()

  val learnAllText = localizationService.createProperty(lifetime, { it.learnAll })

  fun learnAll() {
    flowManager.start(distributionService.getDistribution(), defaultDuration)
  }
}