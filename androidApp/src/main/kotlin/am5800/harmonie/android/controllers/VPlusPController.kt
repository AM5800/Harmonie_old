package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.controllers.util.bindEnabled
import am5800.harmonie.android.controllers.util.bindOnClick
import am5800.harmonie.android.controllers.util.bindText
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.VPlusPViewModel
import android.widget.Button
import android.widget.TextView

class VPlusPController(private val vm: VPlusPViewModel,
                       flowContentController: FlowController,
                       lifetime: Lifetime) : BindableController {
  override val id = R.layout.vplusp
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val question = view.getChild<TextView>(R.id.question)
    question.bindText(bindingLifetime, view, vm.question)

    val btn0 = view.getChild<Button>(R.id.btn0)
    val btn1 = view.getChild<Button>(R.id.btn1)
    val btn2 = view.getChild<Button>(R.id.btn2)

    btn0.bindText(bindingLifetime, view, vm.button0Title)
    btn1.bindText(bindingLifetime, view, vm.button1Title)
    btn2.bindText(bindingLifetime, view, vm.button2Title)

    btn0.bindEnabled(bindingLifetime, view, vm.button0Enabled)
    btn1.bindEnabled(bindingLifetime, view, vm.button1Enabled)
    btn2.bindEnabled(bindingLifetime, view, vm.button2Enabled)

    btn0.bindOnClick(bindingLifetime, { vm.submit(0) })
    btn1.bindOnClick(bindingLifetime, { vm.submit(1) })
    btn2.bindOnClick(bindingLifetime, { vm.submit(2) })
  }

  init {
    vm.activationRequested.subscribe(lifetime, { flowContentController.setContent(this) })
  }
}