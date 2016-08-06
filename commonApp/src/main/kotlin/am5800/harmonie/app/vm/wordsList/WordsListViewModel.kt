package am5800.harmonie.app.vm.wordsList

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.flow.LemmasOrderer
import am5800.harmonie.app.model.localization.LocalizationService
import am5800.harmonie.app.model.repetition.LemmaRepetitionService
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.vm.ViewModelBase

class WordsListViewModel(lifetime: Lifetime,
                         private val sentenceAndLemmasProvider: SentenceAndLemmasProvider,
                         private val lemmaRepetitionService: LemmaRepetitionService,
                         private val orderer: LemmasOrderer,
                         private val localizationService: LocalizationService) : ViewModelBase(lifetime) {
  val items = Property<List<WordsListItemViewModel>>(lifetime, emptyList())
  private var allItems = emptyList<WordsListItemViewModel>()
  private var filter: String = ""
  val scrollPosition = Property<Int>(lifetime, 0)

  private val hardcodedLanguage = Language.German // TODO

  private fun reorder(lemmas: List<Lemma>) {

    val lemmasWithDueDate = lemmas.map { Pair(it, lemmaRepetitionService.tryGetDueDate(it)) }
    val onLearning = lemmasWithDueDate.filter { it.second != null }.sortedByDescending { it.second }
    val notStarted = orderer.reorder(lemmasWithDueDate.filter { it.second == null }.map { it.first })

    val onLearningVms = onLearning.map { OnLearningWordsListItemViewModel(it.first, it.second!!, localizationService) }.toList<WordsListItemViewModel>()
    val nonStartedVms = notStarted.mapIndexed { i, lemma -> NotStartedWordsListItemViewModel(lemma, this, i + 1) }.toList<WordsListItemViewModel>()
    val separator: WordsListItemViewModel = SeparatorWordsListItemViewModel("not started:")

    val result = onLearningVms
        .plus(separator)
        .plus(nonStartedVms)

    allItems = result
  }

  fun search(query: String) {
    filter = query
    applyFilter()
  }

  private fun applyFilter() {
    if (filter.isNullOrEmpty()) items.value = allItems
    else items.value = allItems.filter {
      if (it is SeparatorWordsListItemViewModel) true
      else if (it is NotStartedWordsListItemViewModel) it.lemma.lemma.contains(filter, true)
      else if (it is OnLearningWordsListItemViewModel) it.lemma.lemma.contains(filter, true)
      else throw Exception("Unknown type: " + it.javaClass.name)
    }

    scrollPosition.value = items.value.indexOfFirst { it is SeparatorWordsListItemViewModel }
  }

  fun pullUp(lemma: Lemma) {
    orderer.pullUp(lemma)
    reorder(sentenceAndLemmasProvider.getAllLemmasSorted(hardcodedLanguage))
    applyFilter()
  }

  fun onActivated() {
    filter = ""
    reorder(sentenceAndLemmasProvider.getAllLemmasSorted(hardcodedLanguage))
    applyFilter()
  }
}