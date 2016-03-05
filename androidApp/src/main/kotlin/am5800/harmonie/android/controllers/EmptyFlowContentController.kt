package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.ReflectionBindableController
import am5800.harmonie.app.model.flow.FlowManager


class EmptyFlowContentController(flowController: FlowController,
                                 flowManager: FlowManager,
                                 lifetime: Lifetime) : ReflectionBindableController(R.layout.flow_empty) {
  init {
    flowManager.isEmptySignal.subscribe(lifetime, {
      flowController.setContent(this)
    })
  }
}