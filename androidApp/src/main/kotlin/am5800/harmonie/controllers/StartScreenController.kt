package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.controllers.defaultControls.ButtonController
import am5800.harmonie.model.newest.FlowItemProviderRegistrar
import am5800.harmonie.model.newest.FlowManager
import am5800.harmonie.model.newest.FlowSettings
import am5800.harmonie.viewBinding.ReflectionBindableController
import utils.Lifetime

class StartScreenController(
    flowManager: FlowManager,
    private val providerRegistrar: FlowItemProviderRegistrar, lifetime: Lifetime) : ReflectionBindableController(R.layout.start_screen) {

  val startLearningButton: ButtonController = ButtonController(R.id.startLearningBtn, lifetime, "Learn!")

  init {
    startLearningButton.clickedSignal.subscribe(lifetime, {
      flowManager.start(providerRegistrar.all, FlowSettings(), null/*Minutes.minutes(10).toStandardDuration()*/)
    })
  }
}


