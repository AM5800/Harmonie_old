package am5800.harmonie.android.controllers.workspace

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.controllers.util.bindText
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.model.localization.LocalizationService
import am5800.harmonie.app.vm.workspace.LanguageWorkspaceItemViewModel
import android.widget.TextView

class LanguageWorkspaceItemController(private val vm: LanguageWorkspaceItemViewModel,
                                      private val localizationService: LocalizationService) : BindableController {
  override val id = R.layout.language_workspace_item

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val title = view.getChild<TextView>(R.id.title)
    val onDue = view.getChild<TextView>(R.id.onDue)
    val onLearning = view.getChild<TextView>(R.id.onLearning)

    title.text = vm.title
    val stats = vm.computeBriefStats()
    onDue.bindText(bindingLifetime, view, localizationService.createFormatProperty({ it.onDue }, bindingLifetime, stats.onDue.toString()))
    onLearning.bindText(bindingLifetime, view, localizationService.createFormatProperty({ it.onLearning }, bindingLifetime, stats.onLearning.toString(), stats.total.toString()))
  }

  override fun onClicked() {
    vm.action()
  }
}