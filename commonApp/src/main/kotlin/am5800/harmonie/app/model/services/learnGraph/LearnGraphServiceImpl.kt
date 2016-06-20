package am5800.harmonie.app.model.services.learnGraph

import am5800.common.Sentence
import am5800.common.Word

class LearnGraphServiceImpl : LearnGraphService {
  override fun unlockNextWordsGroup(): List<Word> {
    throw UnsupportedOperationException()
  }

  override fun canUnlockNextWord(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun getUnlockedWords(): List<Word> {
    throw UnsupportedOperationException()
  }

  override fun getUnlockedSentences(): List<Sentence> {
    throw UnsupportedOperationException()
  }
}