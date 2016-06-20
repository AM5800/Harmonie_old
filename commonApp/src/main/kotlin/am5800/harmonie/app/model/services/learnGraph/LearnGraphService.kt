package am5800.harmonie.app.model.services.learnGraph

import am5800.common.Sentence
import am5800.common.Word

interface LearnGraphService {
  fun getUnlockedSentences() : List<Sentence>
  fun getUnlockedWords() : List<Word>
  fun unlockNextWordsGroup() : List<Word>

  fun canUnlockNextWord(): Boolean
}