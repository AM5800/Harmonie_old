package am5800.harmonie.controllers

import am5800.harmonie.ControllerStack
import am5800.harmonie.R
import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.viewBinding.BindableView
import am5800.harmonie.viewBinding.ReflectionBindableController
import android.widget.LinearLayout
import utils.Lifetime
import utils.Property

class DefaultFlowController(private val stack: ControllerStack, lifetime: Lifetime) : FlowController, ReflectionBindableController(R.layout.flow_fragment) {
  private val content = Property<BindableController?>(lifetime, null)

  override fun setContent(controller: BindableController) {
    stack.bringToFront(this)
    content.value = controller
  }

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    super.bind(view, bindingLifetime)

    val placeholder = view.getChild<LinearLayout>(R.id.placeholder)
    content.bindNotNull(bindingLifetime, {
      placeholder.removeAllViews()
      placeholder.addView(view.createViewAndBind(it!!, bindingLifetime))
    })
  }
}