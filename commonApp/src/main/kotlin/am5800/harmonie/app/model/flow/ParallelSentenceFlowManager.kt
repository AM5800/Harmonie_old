package am5800.harmonie.app.model.flow

import am5800.common.Language
import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.common.utilityFunctions.shuffle
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.dbAccess.SentenceAttemptsManager
import am5800.harmonie.app.model.dbAccess.SentenceProvider
import am5800.harmonie.app.model.dbAccess.WordsProvider
import am5800.harmonie.app.model.logging.LoggerProvider

class ParallelSentenceFlowManager(lifetime: Lifetime,
                                  private val sentenceProvider: SentenceProvider,
                                  private val wordsProvider: WordsProvider,
                                  loggerProvider: LoggerProvider,
                                  private val attempts: SentenceAttemptsManager) : FlowItemProvider {
  val question = Property<Pair<Sentence, Sentence>?>(lifetime, null)
  private val logger = loggerProvider.getLogger(javaClass)

  override fun tryPresentNextItem(flowSettings: FlowSettings): Boolean {
    val ich = wordsProvider.tryFindWord("ich", Language.German)!!
    val pair = sentenceProvider.getSentencesWithAnyOfWords(Language.German, Language.English, listOf(ich)).shuffle().first()
    question.value = pair
    return true
  }

  fun submitScore(score: ParallelSentenceUserScore) {
    val sentence = question.value!!.first
    val wordsInSentence = wordsProvider.getWordsInSentence(sentence)

    attempts.submitAttempt(score, sentence)

    for (word in wordsInSentence) {
      val pt = ParallelSentenceUserScore.values()
          .map { tag -> Pair(tag, computePTagGivenWord(word, tag)) }
          .toMap()
      val p = pt[ParallelSentenceUserScore.Good]!! + pt[ParallelSentenceUserScore.NotSure]!! / 2

      logger.info("${word.lemma}: $p")
    }
  }

  private fun computePTagGivenWord(word: Word, tag: ParallelSentenceUserScore): Double {
    val pWordGivenTag = attempts.getWordFrequency(word, tag)
    val tagFrequency = attempts.getTagFrequency(tag)
    val wordFrequency = ParallelSentenceUserScore.values().map { tag ->
      attempts.getWordFrequency(word, tag) * attempts.getTagFrequency(tag)
    }.sum()

    return pWordGivenTag * tagFrequency / wordFrequency
  }
}