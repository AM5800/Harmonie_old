package am5800.harmonie.app.vm.workspace

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.fire
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.ReadonlyProperty
import am5800.harmonie.app.model.exercises.vplusp.VPlusPFlowItemTag
import am5800.harmonie.app.model.feedback.FeedbackService
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.localization.LocalizationService
import am5800.harmonie.app.model.parallelSentence.ParallelSentenceTag
import am5800.harmonie.app.model.workspace.TagStatisticsProvider
import am5800.harmonie.app.vm.ViewModelBase
import am5800.harmonie.app.vm.wordsList.WordsListViewModel

class WorkspaceViewModel(private val lifetime: Lifetime,
                         private val flowManager: FlowManager,
                         private val tagStatisticsProvider: TagStatisticsProvider,
                         private val feedbackService: FeedbackService,
                         private val wordsListViewModel: WordsListViewModel,
                         private val localizationService: LocalizationService) : ViewModelBase(lifetime) {
  private val _items = Property(lifetime, createDefaultItems())
  val items: ReadonlyProperty<Collection<WorkspaceItemViewModel>>
    get() = _items

  private fun createDefaultItems(): Collection<WorkspaceItemViewModel> {
    val all = LanguageWorkspaceItemViewModel("Learn all",
        listOf(ParallelSentenceTag(Language.German)),
        tagStatisticsProvider,
        flowManager)

    val vplusp = LanguageWorkspaceItemViewModel("V+P",
        listOf(VPlusPFlowItemTag()),
        tagStatisticsProvider,
        flowManager)

    val wordsTitle = localizationService.createProperty(lifetime, { it.wordsList })
    val wordsDescription = localizationService.createProperty(lifetime, { it.wordsListDescription })
    val words = SimpleWorkspaceItemViewModel(wordsTitle, wordsDescription, { wordsListViewModel.activationRequested.fire() })

    val feedbackTitle = localizationService.createProperty(lifetime, { it.sendDb })
    val feedbackDescription = localizationService.createProperty(lifetime, { it.sendDbDescription })
    val feedback = SimpleWorkspaceItemViewModel(feedbackTitle, feedbackDescription, { feedbackService.collectAndSendData() })

    return listOf(all, feedback, words, vplusp)
  }
}