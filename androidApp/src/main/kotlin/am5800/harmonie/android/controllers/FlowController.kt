package am5800.harmonie.android.controllers

import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.ControllerWithMenu

interface FlowController {
  fun setContent(controller: ControllerWithMenu)
  fun setContent(controller: BindableController)
}