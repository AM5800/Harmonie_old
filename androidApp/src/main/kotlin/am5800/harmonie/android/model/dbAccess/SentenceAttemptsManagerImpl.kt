package am5800.harmonie.android.model.dbAccess

import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.harmonie.app.model.dbAccess.SentenceAttemptsManager
import am5800.harmonie.app.model.dbAccess.WordsProvider
import am5800.harmonie.app.model.flow.ParallelSentenceUserScore
import java.util.*

class SentenceAttemptsManagerImpl(private val wordsProvider: WordsProvider) : SentenceAttemptsManager {
  var sentences = 0
  val sentenceCountByTag = mutableMapOf<ParallelSentenceUserScore, Int>()
  val words = mutableMapOf<Word, LinkedHashMap<ParallelSentenceUserScore, Int>>()
  val wordsCountByTag = mutableMapOf<ParallelSentenceUserScore, Int>()


  override fun submitAttempt(tag: ParallelSentenceUserScore, sentence: Sentence) {
    ++sentences
    sentenceCountByTag[tag] = (sentenceCountByTag[tag] ?: 0) + 1

    for (word in wordsProvider.getWordsInSentence(sentence)) {
      wordsCountByTag[tag] = (wordsCountByTag[tag] ?: 0) + 1
      val map = words[word] ?: LinkedHashMap()
      map[tag] = (map[tag] ?: 0) + 1
      words[word] = map
    }
  }

  override fun getWordFrequency(word: Word, tag: ParallelSentenceUserScore): Double {
    val wordMap = words[word] ?: return 0.0
    val occurrences = wordMap[tag] ?: return 0.0
    val nWordsWithTag = wordsCountByTag[tag] ?: return 0.0

    return occurrences.toDouble() / nWordsWithTag
  }

  override fun getTagFrequency(tag: ParallelSentenceUserScore): Double {
    return (sentenceCountByTag[tag] ?: 0).toDouble() / sentences
  }
}