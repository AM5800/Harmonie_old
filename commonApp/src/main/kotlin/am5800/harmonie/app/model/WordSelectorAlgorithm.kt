package am5800.harmonie.app.model

import am5800.common.db.Word
import am5800.common.utils.functions.clamp
import am5800.harmonie.app.model.repetition.LearnScore

data class WordScore(val word: Word, val score: LearnScore)


class WordSelectorAlgorithm {
  companion object {
    private fun getFirstUnknownAfter(index: Int, orderedScores: List<WordScore>): Word? {
      return orderedScores.drop(index).firstOrNull { it.score == LearnScore.Unknown }?.word
    }

    fun selectNextWord(orderedScores: List<WordScore>, previouslyChosenWord: Word?, averageUserScore: Double, baseSpeed: Int = 50): Word? {
      if (orderedScores.isEmpty()) throw Exception("Input is empty")
      if (previouslyChosenWord == null) return orderedScores.first().word
      val speed = (baseSpeed * averageUserScore).toInt().clamp(1, baseSpeed)
      val startIndex = orderedScores.indexOfFirst { it.word == previouslyChosenWord }

      var i = startIndex + 1
      var unknownsNumber = 0
      while (i < orderedScores.size && unknownsNumber < speed) {
        val value = orderedScores[i]
        if (value.score == LearnScore.Bad) return getFirstUnknownAfter((i - startIndex) / 2 + startIndex, orderedScores)
        if (value.score == LearnScore.Unknown) ++unknownsNumber
        ++i
      }

      if (i == orderedScores.size && orderedScores.last().score != LearnScore.Unknown) return null

      return orderedScores[i - 1].word
    }
  }
}