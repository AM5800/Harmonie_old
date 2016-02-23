package am5800.harmonie.controllers

import am5800.harmonie.model.FlowItemResult
import am5800.harmonie.model.util.Property
import am5800.harmonie.viewBinding.BindableController

interface FlowItemController : BindableController {
  val result: Property<FlowItemResult>
}