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
    val learnBtn = view.getChild<Button>(R.id.learnAllBtn)
    learnBtn.bindText(bindingLifetime, view, viewModel.learnAllText)
    learnBtn.bindOnClick(bindingLifetime, { viewModel.learnAll() })

    val chooseLanguagesBtn = view.getChild<Button>(R.id.chooseLanguagesBtn)
    chooseLanguagesBtn.bindText(bindingLifetime, view, viewModel.chooseLanguagesText)
    chooseLanguagesBtn.bindOnClick(bindingLifetime, { viewModel.chooseLanguages() })
  }

  override val id: Int = R.layout.start_screen

  init {
    viewModel.activationRequested.subscribe(lifetime, {
      stack.push(this, javaClass.name)
      viewModel.onActivated()
    })
  }
}


