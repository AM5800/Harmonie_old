package am5800.harmonie.app.vm.workspace

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.ReadonlyProperty
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceTag
import am5800.harmonie.app.model.services.flow.FlowManager
import am5800.harmonie.app.model.services.workspace.TagStatisticsProvider
import am5800.harmonie.app.vm.ViewModelBase

class WorkspaceViewModel(lifetime: Lifetime,
                         private val flowManager: FlowManager,
                         private val tagStatisticsProvider: TagStatisticsProvider) : ViewModelBase(lifetime) {
  private val _items = Property(lifetime, createDefaultItems())
  val items: ReadonlyProperty<Collection<SimpleWorkspaceItemViewModel>>
    get() = _items

  private fun createDefaultItems(): Collection<SimpleWorkspaceItemViewModel> {
    val all = SimpleWorkspaceItemViewModel("Learn all",
        listOf(ParallelSentenceTag(Language.German)),
        tagStatisticsProvider,
        flowManager)


    return listOf(all)
  }
}