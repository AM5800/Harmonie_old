package am5800.harmonie.android.controllers.workspace

import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.app.vm.workspace.SimpleWorkspaceItemViewModel


class SimpleWorkspaceItemController(private val vm: SimpleWorkspaceItemViewModel) : BindableController {
  override val id = R.layout.simple_workspace_item
  override fun onClicked() {
    vm.action()
  }
}