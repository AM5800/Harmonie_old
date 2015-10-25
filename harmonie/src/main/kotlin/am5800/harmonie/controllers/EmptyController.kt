package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.viewBinding.ReflectionBindableController
import am5800.harmonie.model.FlowItemResult
import am5800.harmonie.model.util.Property

public class EmptyController : ReflectionBindableController(R.layout.flow_empty), FlowItemController {
    override val result: Property<FlowItemResult> = Property(null)
}