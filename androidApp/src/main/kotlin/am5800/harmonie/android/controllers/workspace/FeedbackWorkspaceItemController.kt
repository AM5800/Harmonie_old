package am5800.harmonie.android.controllers.workspace

import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.app.vm.workspace.WorkspaceItemViewModel


class FeedbackWorkspaceItemController(private val vm: WorkspaceItemViewModel) : BindableController {
  override val id = R.layout.send_feedback_workspace_item
  override fun onClicked() {
    vm.action()
  }
}