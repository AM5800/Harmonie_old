package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.NullableProperty
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.onChangeNotNull
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.controllers.util.bindText
import am5800.harmonie.android.controllers.util.bindVisibility
import am5800.harmonie.android.viewBinding.*
import am5800.harmonie.app.vm.DefaultFlowControllerOwnerViewModel
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class DefaultFlowControllerOwner(private val stack: ControllerStack,
                                 lifetime: Lifetime,
                                 private val vm: DefaultFlowControllerOwnerViewModel) : FlowController, FragmentController, ControllerWithMenu {
  override fun setContent(controller: BindableController) {
    stack.push(this, javaClass.name)
    content.value = controller
    menuItems.value = emptyList()
  }

  override fun tryClose(): Boolean {
    vm.stop()
    return true
  }

  override val menuItems: Property<List<MenuItem>> = Property(lifetime, emptyList<MenuItem>())
  private val content = NullableProperty<BindableController>(lifetime, null)
  override val id: Int = R.layout.flow_fragment

  override fun setContent(controller: ControllerWithMenu) {
    stack.push(this, javaClass.name)
    content.value = controller
    menuItems.value = controller.menuItems.value
  }

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val statusGroup = view.getChild<View>(R.id.statusGroup)
    val statusMessage = view.getChild<TextView>(R.id.statusTextView)

    statusGroup.bindVisibility(bindingLifetime, view, vm.statusVisibility, Visibility.Collapsed)
    statusMessage.bindText(bindingLifetime, view, vm.timeString)

    val placeholder = view.getChild<LinearLayout>(R.id.placeholder)
    content.onChangeNotNull(bindingLifetime, {
      placeholder.removeAllViews()
      placeholder.addView(view.createChildViewAndBind(it, bindingLifetime))
    })
  }
}



