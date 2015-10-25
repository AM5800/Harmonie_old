package am5800.harmonie.controllers.defaultControls

import am5800.harmonie.toVisible
import am5800.harmonie.viewBinding.BindableView
import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.controllers.Visibility
import am5800.harmonie.model.Lifetime
import am5800.harmonie.model.util.Property
import android.text.Html
import android.text.Spanned
import android.widget.Button
import android.widget.TextView


public class TextViewController(override public val id : Int,
                               title : String = "",
                               visible: Visibility = Visibility.Visible,
                               enabled: Boolean = true) : BindableController {
    override fun bind(view: BindableView, bindingLifetime: Lifetime) {
        val tv = view.getChild<TextView>(id)
        title.bindNotNull(bindingLifetime, { view.runOnUiThread ({ tv.setText(it) }) })
        spannedTitle.bindNotNull(bindingLifetime, { view.runOnUiThread ({ tv.setText(it) }) })
        enabled.bindNotNull(bindingLifetime, { view.runOnUiThread ({ tv.setEnabled(it) }) })
        visible.bindNotNull(bindingLifetime, { view.runOnUiThread ({ tv.setVisibility(it.toVisible()) }) })
    }

    val visible: Property<Visibility> = Property(visible)
    val enabled: Property<Boolean> = Property(enabled)
    val title: Property<String> = Property(title)
    val spannedTitle: Property<Spanned> = Property(null)
}