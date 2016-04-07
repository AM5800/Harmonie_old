package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.ObservableCollection
import am5800.harmonie.app.vm.WelcomeScreenViewModel
import am5800.harmonie.app.vm.mapObservable
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class WelcomeScreenController(private val vm: WelcomeScreenViewModel,
                              private val lifetime: Lifetime,
                              private val controllerStack: ControllerStack) : BindableController {
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    // always visible group
    view.getChild<TextView>(R.id.welcomeView).bindText(bindingLifetime, view, vm.welcome)
    view.getChild<TextView>(R.id.chooseKnownView).bindText(bindingLifetime, view, vm.chooseKnown)
    val knownControllers: ObservableCollection<BindableController> = vm.knownLanguages.mapObservable { CheckableLanguageController(R.layout.checkable_language, it) }
    bindLinearLayoutToObservableCollection(bindingLifetime, view.getChild<LinearLayout>(R.id.knownLanguagesList), view, knownControllers)

    // learn group
    val chooseLearnText = view.getChild<TextView>(R.id.chooseLearnView)
    val learnList = view.getChild<LinearLayout>(R.id.learnLanguagesList)
    chooseLearnText.bindText(bindingLifetime, view, vm.chooseLearn)
    chooseLearnText.bindVisibility(bindingLifetime, view, vm.learnGroupVisible, Visibility.Collapsed)
    learnList.bindVisibility(bindingLifetime, view, vm.learnGroupVisible, Visibility.Collapsed)

    val learnControllers: ObservableCollection<BindableController> = vm.learnLanguages.mapObservable { CheckableLanguageController(R.layout.checkable_language, it) }
    bindLinearLayoutToObservableCollection(bindingLifetime, learnList, view, learnControllers)

    // continue
    val continueBtn = view.getChild<Button>(R.id.continueBtn)
    continueBtn.bindText(bindingLifetime, view, vm.continueBtnText)
    continueBtn.bindVisibility(bindingLifetime, view, vm.continueBtnVisible, Visibility.Collapsed)
    continueBtn.bindOnClick(bindingLifetime, { vm.next() })
  }

  private fun <T : BindableController> bindLinearLayoutToObservableCollection(lifetime: Lifetime, layout: LinearLayout, parentView: BindableView, collection: ObservableCollection<T>) {
    val handler: (Unit) -> Unit = {
      layout.removeAllViews()
      for (item in collection) {
        layout.addView(parentView.createChildViewAndBind(item, lifetime))
      }
    }

    collection.changed.subscribe(lifetime, handler)
    handler(Unit)
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