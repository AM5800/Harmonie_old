package am5800.harmonie.app.vm.wordsList

import am5800.common.Lemma

class NotStartedWordsListItemViewModel(lemma: Lemma) : WordsListItemViewModel {
  override val title = lemma.lemma
}