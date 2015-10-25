package am5800.harmonie.viewBinding

import am5800.harmonie.model.Lifetime
import am5800.harmonie.viewBinding.BindableView

public interface BindableController {
    public fun bind(view: BindableView, bindingLifetime: Lifetime)
    public val id : Int

    fun onActivated() {}
}

