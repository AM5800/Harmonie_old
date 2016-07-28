package am5800.harmonie.app.vm.wordsList

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.flow.LemmasLearnOrderer
import am5800.harmonie.app.model.repetition.LemmaRepetitionService
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.vm.ViewModelBase

class WordsListViewModel(lifetime: Lifetime,
                         private val sentenceAndLemmasProvider: SentenceAndLemmasProvider,
                         private val lemmaRepetitionService: LemmaRepetitionService,
                         private val orderer: LemmasLearnOrderer) : ViewModelBase(lifetime) {
  val items = Property<List<WordsListItemViewModel>>(lifetime, emptyList())

  private val hardcodedLanguage = Language.German // TODO

  init {
    update(sentenceAndLemmasProvider.getAllLemmas(hardcodedLanguage), hardcodedLanguage)
  }

  private fun update(lemmas: List<Lemma>, language: Language) {
    val attemptedLemmas = lemmaRepetitionService.getAttemptedLemmas(language)

    val onLearning = lemmas.intersect(attemptedLemmas)
    val notStarted = orderer.reorder(lemmas.minus(onLearning))

    val onLearningVms = onLearning.map { OnLearningWordsListItemViewModel(it) }.toList<WordsListItemViewModel>()
    val nonStartedVms = notStarted.map { NotStartedWordsListItemViewModel(it) }.toList<WordsListItemViewModel>()
    val separator: WordsListItemViewModel = SeparatorWordsListItemViewModel("not started:")

    val result = onLearningVms
        .plus(separator)
        .plus(nonStartedVms)

    items.value = result
  }

  fun search(query: String) {

  }

  fun pullUp(lemma: Lemma) {

  }

  fun markKnown(lemma: Lemma) {

  }
}