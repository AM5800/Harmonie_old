package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.StartScreenViewModel
import android.widget.Button

class StartScreenController(private val viewModel: StartScreenViewModel, lifetime: Lifetime, stack: ControllerStack) : BindableController {

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val learnButton = view.getChild<Button>(R.id.learnAll)
    learnButton.bindText(bindingLifetime, view, viewModel.learnAllText)

    learnButton.bindOnClick(bindingLifetime, {
      viewModel.learnAll()
    })
  }

  override val id: Int = R.layout.start_screen

  init {
    viewModel.activationRequired.subscribe(lifetime, {
      stack.push(this, javaClass.name)
    })
  }
}


