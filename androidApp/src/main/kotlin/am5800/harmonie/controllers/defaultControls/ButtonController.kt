package am5800.harmonie.controllers.defaultControls

import Lifetime
import Property
import am5800.harmonie.controllers.Visibility
import am5800.harmonie.toVisible
import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.viewBinding.BindableView
import android.widget.Button

class ButtonController(
        override val id: Int,
        private val onClicked: () -> Unit,
        title: String = "",
        visible: Visibility = Visibility.Visible,
        enabled: Boolean = true) : BindableController {

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val btn = view.getChild<Button>(id)
    title.bindNotNull(bindingLifetime, { view.runOnUiThread ({ btn.text = it }) })
    enabled.bindNotNull(bindingLifetime, { view.runOnUiThread ({ btn.isEnabled = it }) })
    visible.bindNotNull(bindingLifetime, { view.runOnUiThread ({ btn.visibility = it.toVisible() }) })
    btn.setOnClickListener { view.runOnUiThread { clicked () } }
  }

  val visible: Property<Visibility> = Property(visible)
  val enabled: Property<Boolean> = Property(enabled)
  val title: Property<String> = Property(title)
  fun clicked() = onClicked()
}



