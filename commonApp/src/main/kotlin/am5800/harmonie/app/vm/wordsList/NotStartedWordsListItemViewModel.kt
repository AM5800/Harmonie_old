package am5800.harmonie.app.vm.wordsList

import am5800.common.Lemma

class NotStartedWordsListItemViewModel(val lemma: Lemma,
                                       private val wordsListViewModel: WordsListViewModel,
                                       val order: Int) : WordsListItemViewModel {
  val title = lemma.lemma

  fun pullUp() {
    wordsListViewModel.pullUp(lemma)
  }
}