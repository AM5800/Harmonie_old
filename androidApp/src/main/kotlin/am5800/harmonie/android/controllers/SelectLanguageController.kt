package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.android.viewBinding.FragmentController
import am5800.harmonie.app.vm.CheckableLanguageViewModel
import am5800.harmonie.app.vm.SelectLanguageViewModel
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SelectLanguageController(private val vm: SelectLanguageViewModel,
                               private val lifetime: Lifetime,
                               private val controllerStack: ControllerStack) : FragmentController {
  override val menuItems = null

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    // always visible group
    view.getChild<TextView>(R.id.welcomeView).bindText(bindingLifetime, view, vm.welcome)
    view.getChild<TextView>(R.id.chooseKnownView).bindText(bindingLifetime, view, vm.chooseKnown)
    bindControllers(view.getChild<LinearLayout>(R.id.knownLanguagesList), view, bindingLifetime, vm.knownLanguages)

    // learn group
    val chooseLearnText = view.getChild<TextView>(R.id.chooseLearnView)
    val learnList = view.getChild<LinearLayout>(R.id.learnLanguagesList)
    chooseLearnText.bindText(bindingLifetime, view, vm.chooseLearn)
    chooseLearnText.bindVisibility(bindingLifetime, view, vm.learnGroupVisible, Visibility.Collapsed)
    learnList.bindVisibility(bindingLifetime, view, vm.learnGroupVisible, Visibility.Collapsed)

    bindControllers(learnList, view, bindingLifetime, vm.learnLanguages)

    // continue
    val continueBtn = view.getChild<Button>(R.id.continueBtn)
    continueBtn.bindText(bindingLifetime, view, vm.continueBtnText)
    continueBtn.bindVisibility(bindingLifetime, view, vm.continueBtnVisible, Visibility.Collapsed)
    continueBtn.bindOnClick(bindingLifetime, { vm.next() })
  }

  private fun bindControllers(layout: LinearLayout, view: BindableView, bindingLifetime: Lifetime, languages: Collection<CheckableLanguageViewModel>) {
    for (language in languages) {
      val controller = CheckableLanguageController(R.layout.checkable_language, language)
      layout.addView(view.createChildViewAndBind(controller, bindingLifetime))
    }
  }

  override val id: Int = R.layout.welcome_screen

  init {
    vm.activationRequested.subscribe(lifetime, {
      controllerStack.push(this, this.javaClass.name, { vm.canCloseNow() })
    })

    vm.closeRequested.subscribe(lifetime, {
      if (controllerStack.top() == this) {
        controllerStack.back()
      }
    })
  }
}