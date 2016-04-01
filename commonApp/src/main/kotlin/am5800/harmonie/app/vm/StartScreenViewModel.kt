package am5800.harmonie.app.vm

import am5800.common.Language
import am5800.harmonie.app.model.flow.FlowItemProviderRegistrar
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.FlowSettings
import org.joda.time.Minutes

class StartScreenViewModel(private val flowManager: FlowManager,
                           private val providerRegistrar: FlowItemProviderRegistrar) {
  private val defaultDuration = Minutes.minutes(10).toStandardDuration()

  fun startLearningDeEn() {
    flowManager.start(providerRegistrar.all, FlowSettings(Language.German, Language.English), defaultDuration)
  }

  fun startLearningEnDe() {
    flowManager.start(providerRegistrar.all, FlowSettings(Language.English, Language.German), defaultDuration)
  }

  fun startLearningJpRu() {
    flowManager.start(providerRegistrar.all, FlowSettings(Language.Japanese, Language.Russian), defaultDuration)
  }
}