package am5800.harmonie.android.controllers.workspace

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.controllers.util.bindText
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.workspace.SimpleWorkspaceItemViewModel
import android.widget.TextView


class SimpleWorkspaceItemController(private val vm: SimpleWorkspaceItemViewModel) : BindableController {
  override val id = R.layout.simple_workspace_item
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val titleView = view.getChild<TextView>(R.id.title)
    val descriptionView = view.getChild<TextView>(R.id.description)

    titleView.bindText(bindingLifetime, view, vm.title)
    descriptionView.bindText(bindingLifetime, view, vm.description)
  }

  override fun onClicked() {
    vm.action()
  }
}