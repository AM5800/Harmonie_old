package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.model.features.flow.FlowManager
import am5800.harmonie.app.model.features.localization.LocalizationService
import android.widget.TextView


class EmptyFlowContentController(flowController: FlowController,
                                 flowManager: FlowManager,
                                 lifetime: Lifetime,
                                 private val localizationService: LocalizationService) : BindableController {
  override val id: Int = R.layout.flow_empty
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val property = localizationService.createProperty(bindingLifetime, { it.lessonIsOver })
    view.getChild<TextView>(R.id.lessonIsOver).bindText(bindingLifetime, view, property)
  }

  init {
    flowManager.isEmptySignal.subscribe(lifetime, {
      flowController.setContent(this)
    })
  }
}