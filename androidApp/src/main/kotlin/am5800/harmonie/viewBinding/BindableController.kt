package am5800.harmonie.viewBinding

import utils.Lifetime

interface BindableController {
  fun bind(view: BindableView, bindingLifetime: Lifetime)
  val id: Int

  fun onActivated() {
  }

  fun onClicked() {

  }
}

