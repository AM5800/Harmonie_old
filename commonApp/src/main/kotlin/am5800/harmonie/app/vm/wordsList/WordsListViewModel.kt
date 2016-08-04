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

  private val hardcodedLanguage = Language.German // TODO

  init {
    update(sentenceAndLemmasProvider.getAllLemmasSorted(hardcodedLanguage), hardcodedLanguage)
  }

  private fun update(lemmas: List<Lemma>, language: Language) {
    val attemptedLemmas = lemmaRepetitionService.getAttemptedLemmas(language)

    val onLearning = lemmas.intersect(attemptedLemmas)
    val notStarted = orderer.reorder(lemmas.minus(onLearning))

    val onLearningVms = onLearning.map { OnLearningWordsListItemViewModel(it) }.toList<WordsListItemViewModel>()
    val nonStartedVms = notStarted.mapIndexed { i, lemma -> NotStartedWordsListItemViewModel(lemma, this, i + 1) }.toList<WordsListItemViewModel>()
    val separator: WordsListItemViewModel = SeparatorWordsListItemViewModel("not started:")

    val result = onLearningVms
        .plus(separator)
        .plus(nonStartedVms)

    items.value = result
  }

  fun search(query: String) {
    update(sentenceAndLemmasProvider.searchLemmas(query, hardcodedLanguage), hardcodedLanguage)
  }

  fun pullUp(lemma: Lemma) {
    orderer.pullUp(lemma)
    update(sentenceAndLemmasProvider.getAllLemmasSorted(hardcodedLanguage), hardcodedLanguage)
  }
}