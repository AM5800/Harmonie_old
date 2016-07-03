package am5800.harmonie.android.controllers.workspace

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.controllers.util.bindText
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.model.features.localization.LocalizationService
import am5800.harmonie.app.vm.workspace.SimpleWorkspaceItemViewModel
import android.widget.TextView

class SimpleWorkspaceItemController(private val vm: SimpleWorkspaceItemViewModel,
                                    private val localizationService: LocalizationService) : BindableController {
  override val id = R.layout.workspace_item

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val header = view.getChild<TextView>(R.id.header)
    val onDue = view.getChild<TextView>(R.id.onDue)
    val onLearning = view.getChild<TextView>(R.id.onLearning)

    header.text = vm.header
    val stats = vm.computeBriefStats()
    onDue.bindText(bindingLifetime, view, localizationService.createFormatProperty({ it.onDue }, bindingLifetime, stats.onDue))
    onLearning.bindText(bindingLifetime, view, localizationService.createFormatProperty({ it.onLearning }, bindingLifetime, stats.onLearning, stats.total))
  }

  override fun onClicked() {
    vm.onCommand()
  }
}