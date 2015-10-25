package am5800.harmonie.controllers.defaultControls

import am5800.harmonie.toVisible
import am5800.harmonie.viewBinding.BindableView
import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.controllers.Visibility
import am5800.harmonie.model.Lifetime
import am5800.harmonie.model.util.Property
import android.widget.Button

public class ButtonController(
        override public val id: Int,
        private val onClicked: () -> Unit,
        title: String = "",
        visible: Visibility = Visibility.Visible,
        enabled: Boolean = true) : BindableController {

    override fun bind(view: BindableView, bindingLifetime: Lifetime) {
        val btn = view.getChild<Button>(id)
        title.bindNotNull(bindingLifetime, { view.runOnUiThread ({ btn.setText(it) }) })
        enabled.bindNotNull(bindingLifetime, { view.runOnUiThread ({ btn.setEnabled(it) }) })
        visible.bindNotNull(bindingLifetime, { view.runOnUiThread ({ btn.setVisibility(it.toVisible()) }) })
        btn.setOnClickListener { view.runOnUiThread { clicked () } }
    }

    val visible: Property<Visibility> = Property(visible)
    val enabled: Property<Boolean> = Property(enabled)
    val title: Property<String> = Property(title)
    fun clicked() = onClicked()
}



