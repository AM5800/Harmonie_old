package am5800.harmonie

import am5800.harmonie.viewBinding.BindableController

interface ViewOpener {
  fun bringToFront(controller: BindableController)
}