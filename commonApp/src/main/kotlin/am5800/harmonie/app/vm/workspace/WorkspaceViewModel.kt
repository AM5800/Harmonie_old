package am5800.harmonie.app.vm.workspace

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.ReadonlyProperty
import am5800.harmonie.app.model.features.fillTheGap.FillTheGapCategory
import am5800.harmonie.app.model.features.flow.FlowManager
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceCategory
import am5800.harmonie.app.vm.ViewModelBase

class WorkspaceViewModel(private val lifetime: Lifetime,
                         private val flowManager: FlowManager) : ViewModelBase(lifetime) {
  private val _items = Property(lifetime, createDefaultItems())
  val items: ReadonlyProperty<Collection<SimpleWorkspaceItemViewModel>>
    get() = _items

  private fun createDefaultItems(): Collection<SimpleWorkspaceItemViewModel> {
    val all = SimpleWorkspaceItemViewModel("Learn all",
        listOf(ParallelSentenceCategory(Language.German, Language.English), FillTheGapCategory(Language.German, Language.English)),
        lifetime,
        flowManager)

    val onlySentences = SimpleWorkspaceItemViewModel("Learn all(without exercises)",
        listOf(ParallelSentenceCategory(Language.German, Language.English)),
        lifetime,
        flowManager)


    return listOf(all, onlySentences)
  }
}