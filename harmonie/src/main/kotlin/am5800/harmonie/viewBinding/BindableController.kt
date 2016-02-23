package am5800.harmonie.viewBinding

import am5800.harmonie.model.Lifetime

interface BindableController {
  fun bind(view: BindableView, bindingLifetime: Lifetime)
  val id: Int

  fun onActivated() {
  }
}

