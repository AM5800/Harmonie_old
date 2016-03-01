package am5800.harmonie.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.R
import am5800.harmonie.model.FlowManager
import am5800.harmonie.viewBinding.ReflectionBindableController


class EmptyFlowContentController(flowController: FlowController,
                                 flowManager: FlowManager,
                                 lifetime: Lifetime) : ReflectionBindableController(R.layout.flow_empty) {
  init {
    flowManager.isEmptySignal.subscribe(lifetime, {
      flowController.setContent(this)
    })
  }
}