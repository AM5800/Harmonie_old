package am5800.harmonie.app.model

import am5800.common.Word
import am5800.common.utils.functions.clamp
import am5800.harmonie.app.model.repetition.LearnScore


class WordSelectorAlgorithm {
  companion object {
    private fun getFirstUnknownAfter(index: Int, orderedWords: List<Word>, getScore: (Word) -> LearnScore?): Word? {
      return orderedWords.drop(index).firstOrNull { getScore(it) == null }
    }

    fun selectNextWord(orderedWords: List<Word>, previouslyChosenWord: Word?, averageUserScore: Double, getScore: (Word) -> LearnScore?, baseSpeed: Int = 50): Word? {
      if (orderedWords.isEmpty()) throw Exception("Input is empty")
      if (previouslyChosenWord == null) return getFirstUnknownAfter(0, orderedWords, getScore)
      val speed = (baseSpeed * averageUserScore).toInt().clamp(1, baseSpeed)
      val startIndex = orderedWords.indexOfFirst { it.lemma == previouslyChosenWord.lemma }
      if (startIndex == -1) return getFirstUnknownAfter(0, orderedWords, getScore)

      var i = startIndex + 1
      var unknownsNumber = 0
      while (i < orderedWords.size && unknownsNumber < speed) {
        val word = orderedWords[i]
        val score = getScore(word)
        if (score == LearnScore.Bad) return getFirstUnknownAfter((i - startIndex) / 2 + startIndex, orderedWords, getScore)
        if (score == null) {
          ++unknownsNumber
          if (i == orderedWords.size - 1) return word
          if (unknownsNumber == speed) return word
        }
        ++i
      }

      return null
    }
  }
}