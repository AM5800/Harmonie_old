package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.controllers.defaultControls.ButtonController
import am5800.harmonie.android.viewBinding.ReflectionBindableController
import am5800.harmonie.app.vm.StartScreenViewModel

class StartScreenController(lifetime: Lifetime,
                            viewModel: StartScreenViewModel) : ReflectionBindableController(R.layout.start_screen) {

  val startLearningButton: ButtonController = ButtonController(R.id.startLearningBtn, lifetime, "Learn!")

  init {
    startLearningButton.clickedSignal.subscribe(lifetime, {
      viewModel.startLearning()
    })
  }
}


