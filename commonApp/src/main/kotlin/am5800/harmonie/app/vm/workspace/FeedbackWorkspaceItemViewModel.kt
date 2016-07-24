package am5800.harmonie.app.vm.workspace

import am5800.harmonie.app.model.features.feedback.FeedbackService


class FeedbackWorkspaceItemViewModel(private val feedbackService: FeedbackService) : WorkspaceItemViewModel {
  override fun action() {
    feedbackService.collectAndSendData()
  }
}