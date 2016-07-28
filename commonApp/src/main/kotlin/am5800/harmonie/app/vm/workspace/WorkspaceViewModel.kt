package am5800.harmonie.app.vm.workspace

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.fire
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.ReadonlyProperty
import am5800.harmonie.app.model.feedback.FeedbackService
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.parallelSentence.ParallelSentenceTag
import am5800.harmonie.app.model.workspace.TagStatisticsProvider
import am5800.harmonie.app.vm.ViewModelBase
import am5800.harmonie.app.vm.wordsList.WordsListViewModel

class WorkspaceViewModel(lifetime: Lifetime,
                         private val flowManager: FlowManager,
                         private val tagStatisticsProvider: TagStatisticsProvider,
                         private val feedbackService: FeedbackService,
                         private val wordsListViewModel: WordsListViewModel) : ViewModelBase(lifetime) {
  private val _items = Property(lifetime, createDefaultItems())
  val items: ReadonlyProperty<Collection<WorkspaceItemViewModel>>
    get() = _items

  private fun createDefaultItems(): Collection<WorkspaceItemViewModel> {
    val all = LanguageWorkspaceItemViewModel("Learn all",
        listOf(ParallelSentenceTag(Language.German)),
        tagStatisticsProvider,
        flowManager)

    val words = SimpleWorkspaceItemViewModel({ it.wordsList }, { it.wordsListDescription }, { wordsListViewModel.activationRequested.fire() })

    val feedback = SimpleWorkspaceItemViewModel({ it.sendDb }, { it.sendDbDescription }, { feedbackService.collectAndSendData() })


    return listOf(all, feedback, words)
  }
}