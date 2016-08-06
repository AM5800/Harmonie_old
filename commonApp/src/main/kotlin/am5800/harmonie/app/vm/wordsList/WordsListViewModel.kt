package am5800.harmonie.app.vm.wordsList

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.flow.LemmasOrderer
import am5800.harmonie.app.model.repetition.LemmaRepetitionService
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.vm.ViewModelBase

class WordsListViewModel(lifetime: Lifetime,
                         private val sentenceAndLemmasProvider: SentenceAndLemmasProvider,
                         private val lemmaRepetitionService: LemmaRepetitionService,
                         private val orderer: LemmasOrderer) : ViewModelBase(lifetime) {
  val items = Property<List<WordsListItemViewModel>>(lifetime, emptyList())
  private var allItems = emptyList<WordsListItemViewModel>()
  private var filter: String = ""

  private val hardcodedLanguage = Language.German // TODO

  private fun reorder(lemmas: List<Lemma>, language: Language) {
    val attemptedLemmas = lemmaRepetitionService.getAttemptedLemmas(language)

    val onLearning = lemmas.intersect(attemptedLemmas)
    val notStarted = orderer.reorder(lemmas.minus(onLearning))

    val onLearningVms = onLearning.map { OnLearningWordsListItemViewModel(it) }.toList<WordsListItemViewModel>()
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
  }

  fun pullUp(lemma: Lemma) {
    orderer.pullUp(lemma)
    reorder(sentenceAndLemmasProvider.getAllLemmasSorted(hardcodedLanguage), hardcodedLanguage)
    applyFilter()
  }

  fun onActivated() {
    filter = ""
    reorder(sentenceAndLemmasProvider.getAllLemmasSorted(hardcodedLanguage), hardcodedLanguage)
    applyFilter()
  }
}