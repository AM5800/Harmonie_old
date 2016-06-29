package am5800.harmonie.app.model.services.learnGraph

import am5800.common.Language
import am5800.common.Sentence
import am5800.common.Word

interface LearnGraphService {
  fun getUnlockedSentences(learnLanguage: Language): List<Sentence>
  fun getUnlockedWords(learnLanguage: Language): List<Word>
  fun unlockNextWordsGroup(learnLanguage: Language) : List<Word>
  fun canUnlockNextWordGroup(learnLanguage: Language): Boolean
}