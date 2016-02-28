package am5800.harmonie.controllers

import am5800.harmonie.viewBinding.BindableController

interface FlowController {
  fun setContent(controller: BindableController)
}