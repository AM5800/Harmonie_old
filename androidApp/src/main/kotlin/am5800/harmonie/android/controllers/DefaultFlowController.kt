package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.android.viewBinding.ReflectionBindableController
import android.widget.LinearLayout

class DefaultFlowController(private val stack: ControllerStack, lifetime: Lifetime) : FlowController, ReflectionBindableController(R.layout.flow_fragment) {
  private val content = Property<BindableController?>(lifetime, null)

  override fun setContent(controller: BindableController) {
    stack.bringToFront(this, javaClass.name)
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