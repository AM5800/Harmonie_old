package am5800.harmonie.controllers.defaultControls

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.controllers.Visibility
import am5800.harmonie.toVisible
import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.viewBinding.BindableView
import android.text.Spanned
import android.widget.TextView


class TextViewController(override val id: Int,
                         lifetime: Lifetime,
                         title: String = "",
                         visible: Visibility = Visibility.Visible,
                         enabled: Boolean = true) : BindableController {
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val tv = view.getChild<TextView>(id)
    title.bindNotNull(bindingLifetime, { view.runOnUiThread ({ tv.text = it }) })
    spannedTitle.bindNotNull(bindingLifetime, { view.runOnUiThread ({ tv.text = it }) })
    enabled.bindNotNull(bindingLifetime, { view.runOnUiThread ({ tv.isEnabled = it }) })
    visible.bindNotNull(bindingLifetime, { view.runOnUiThread ({ tv.visibility = it.toVisible() }) })
  }

  val visible: Property<Visibility> = Property(lifetime, visible)
  val enabled: Property<Boolean> = Property(lifetime, enabled)
  val title: Property<String> = Property(lifetime, title)
  val spannedTitle: Property<Spanned?> = Property(lifetime, null)
}