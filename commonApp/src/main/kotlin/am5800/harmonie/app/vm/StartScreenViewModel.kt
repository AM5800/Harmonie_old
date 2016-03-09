package am5800.harmonie.app.vm

import am5800.common.Language
import am5800.harmonie.app.model.flow.FlowItemProviderRegistrar
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.FlowSettings
import org.joda.time.Minutes

class StartScreenViewModel(private val flowManager: FlowManager,
                           private val providerRegistrar: FlowItemProviderRegistrar
) {
  fun startLearning() {
    flowManager.start(providerRegistrar.all, FlowSettings(Language.German, Language.English), Minutes.minutes(10).toStandardDuration())
  }
}