package am5800.harmonie.viewBinding

import Lifetime

interface BindableController {
  fun bind(view: BindableView, bindingLifetime: Lifetime)
  val id: Int

  fun onActivated() {
  }
}

