package am5800.harmonie.android.controllers.defaultControls

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.Signal
import am5800.harmonie.android.toVisible
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.Visibility
import android.widget.Button

class ButtonController(
    override val id: Int,
    lifetime: Lifetime,
    title: String = "",
    visible: Visibility = Visibility.Visible,
    enabled: Boolean = true) : BindableController {

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val btn = view.getChild<Button>(id)
    title.bindNotNull(bindingLifetime, { view.runOnUiThread ({ btn.text = it }) })
    enabled.bindNotNull(bindingLifetime, { view.runOnUiThread ({ btn.isEnabled = it }) })
    visible.bindNotNull(bindingLifetime, { view.runOnUiThread ({ btn.visibility = it.toVisible() }) })
    btn.setOnClickListener { view.runOnUiThread { clickedSignal.fire(Unit) } }
  }

  val visible: Property<Visibility> = Property(lifetime, visible)
  val enabled: Property<Boolean> = Property(lifetime, enabled)
  val title: Property<String> = Property(lifetime, title)

  val clickedSignal = Signal<Unit>(lifetime)
}



