package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.app.model.flow.FlowManager


class EmptyFlowContentController(flowController: FlowController,
                                 flowManager: FlowManager,
                                 lifetime: Lifetime) : BindableController {
  override val id: Int = R.layout.flow_empty

  init {
    flowManager.isEmptySignal.subscribe(lifetime, {
      flowController.setContent(this)
    })
  }
}