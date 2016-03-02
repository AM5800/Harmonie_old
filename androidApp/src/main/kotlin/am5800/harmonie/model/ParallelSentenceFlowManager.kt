package am5800.harmonie.model

import am5800.common.Language
import am5800.common.db.DbSentence
import am5800.common.utilityFunctions.shuffle
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.model.dbAccess.DbWord
import am5800.harmonie.model.dbAccess.SentenceProvider
import am5800.harmonie.model.dbAccess.WordsProvider
import am5800.harmonie.model.logging.LoggerProvider
import java.util.*

enum class ParallelSentenceUserScore {
  Good,
  NotSure,
  Bad
}

class ParallelSentenceFlowManager(lifetime: Lifetime,
                                  private val sentenceProvider: SentenceProvider,
                                  private val wordsProvider: WordsProvider,
                                  loggerProvider: LoggerProvider) : FlowItemProvider {
  val question = Property<Pair<DbSentence, DbSentence>?>(lifetime, null)
  private val history = mutableListOf<Pair<DbSentence, ParallelSentenceUserScore>>()
  private val logger = loggerProvider.getLogger(javaClass)

  override fun tryPresentNextItem(flowSettings: FlowSettings): Boolean {
    val pair = sentenceProvider.getSentences(Language.German, Language.English).shuffle().first()
    question.value = pair
    return true
  }

  fun submitScore(score: ParallelSentenceUserScore) {
    val current = question.value!!
    history.add(Pair(current.first, score))

    val allWords = mutableMapOf<DbWord, Int>()
    val tagToWordHistory = mutableMapOf<ParallelSentenceUserScore, LinkedHashMap<DbWord, Int>>()

    for ((sentence, tag) in history) {
      val words = wordsProvider.getWordsInSentence(sentence)
      for (word in words) {
        allWords[word] = (allWords[word] ?: 0) + 1
        val linkedHashMap = tagToWordHistory[tag] ?: LinkedHashMap()
        val wordCount = linkedHashMap[word] ?: 0
        linkedHashMap[word] = wordCount + 1
        tagToWordHistory[tag] = linkedHashMap
      }
    }

    val tagFrequencies = ParallelSentenceUserScore.values()
        .map { tag -> Pair(tag, history.count { it.second == tag }) }
        .map { Pair(it.first, it.second.toDouble() / history.size) }
        .toMap()

    for ((word, occurrences) in allWords) {
      val pFts = ParallelSentenceUserScore.values()
          .map { tag ->
            Pair(tag, (tagToWordHistory[tag]?.get(word)?.toDouble() ?: 0.0) / occurrences)
          }
          .toMap()

      val pt = ParallelSentenceUserScore.values()
          .map { tag -> Pair(tag, computePTagGivenWord(pFts, tag, tagFrequencies)) }
          .toMap()
      val p = pt[ParallelSentenceUserScore.Good]!! + pt[ParallelSentenceUserScore.NotSure]!! / 2

      logger.info("$word: $p")
    }
  }

  private fun computePTagGivenWord(pFts: Map<ParallelSentenceUserScore, Double>, tag: ParallelSentenceUserScore, tagFrequencies: Map<ParallelSentenceUserScore, Double>): Double {
    val pWordGivenTag = pFts[tag]!!
    val tagFrequency = tagFrequencies[tag]!!
    val wordFrequency = ParallelSentenceUserScore.values().map { tag -> pFts[tag]!! * tagFrequencies[tag]!! }.sum()

    return pWordGivenTag * tagFrequency / wordFrequency
  }
}