package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.DefaultFlowControllerOwnerViewModel
import android.content.res.Resources
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class DefaultFlowControllerOwner(private val stack: ControllerStack,
                                 lifetime: Lifetime,
                                 private val vm: DefaultFlowControllerOwnerViewModel,
                                 private val resources: Resources) : FlowController, BindableController {
  private val content = Property<BindableController?>(lifetime, null)
  override val id: Int = R.layout.flow_fragment

  override fun setContent(controller: BindableController) {
    stack.bringToFront(this, javaClass.name)
    content.value = controller
  }

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val statusGroup = view.getChild<View>(R.id.statusGroup)
    val statusMessage = view.getChild<TextView>(R.id.statusTextView)

    statusGroup.bindVisibility(bindingLifetime, view, vm.statusVisibility)
    statusMessage.bindText(bindingLifetime, view, vm.timeLeft, { duration ->
      val minutes = duration.standardMinutes.toInt()
      if (minutes > 0) resources.getQuantityString(R.plurals.minutesLeft, minutes, minutes)
      else {
        val seconds = duration.standardSeconds.toInt()
        resources.getQuantityString(R.plurals.secondsLeft, seconds, seconds)
      }
    })

    val placeholder = view.getChild<LinearLayout>(R.id.placeholder)
    content.bindNotNull(bindingLifetime, {
      placeholder.removeAllViews()
      placeholder.addView(view.createViewAndBind(it!!, bindingLifetime))
    })
  }
}



