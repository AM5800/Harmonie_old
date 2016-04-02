package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.DefaultFlowControllerOwnerViewModel
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class DefaultFlowControllerOwner(private val stack: ControllerStack,
                                 lifetime: Lifetime,
                                 private val vm: DefaultFlowControllerOwnerViewModel) : FlowController, BindableController {
  private val content = Property<BindableController>(lifetime, null)
  override val id: Int = R.layout.flow_fragment

  override fun setContent(controller: BindableController) {
    stack.push(this, javaClass.name)
    content.value = controller
  }

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val statusGroup = view.getChild<View>(R.id.statusGroup)
    val statusMessage = view.getChild<TextView>(R.id.statusTextView)

    statusGroup.bindVisibility(bindingLifetime, view, vm.statusVisibility, Visibility.Collapsed)
    statusMessage.bindText(bindingLifetime, view, vm.timeLeft)

    val placeholder = view.getChild<LinearLayout>(R.id.placeholder)
    content.onChangeNotNull(bindingLifetime, {
      placeholder.removeAllViews()
      placeholder.addView(view.createChildViewAndBind(it, bindingLifetime))
    })
  }
}



