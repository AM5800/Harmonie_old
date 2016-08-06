package am5800.harmonie.app.vm.wordsList

import am5800.common.Lemma

class OnLearningWordsListItemViewModel(val lemma: Lemma) : WordsListItemViewModel {
  val title = lemma.lemma
}