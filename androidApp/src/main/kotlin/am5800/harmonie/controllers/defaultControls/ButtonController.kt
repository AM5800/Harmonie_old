package am5800.harmonie.controllers.defaultControls

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.Signal
import am5800.harmonie.controllers.Visibility
import am5800.harmonie.toVisible
import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.viewBinding.BindableView
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



