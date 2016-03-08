package am5800.harmonie.app.model.flow

import am5800.common.Language
import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.common.utilityFunctions.shuffle
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.dbAccess.AttemptScore
import am5800.harmonie.app.model.dbAccess.AttemptsService
import am5800.harmonie.app.model.dbAccess.SentenceProvider
import am5800.harmonie.app.model.logging.LoggerProvider
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap


data class TextRange(val start: Int, val end: Int)

data class ParallelSentenceQuestion(val question: Sentence, val answer: Sentence, val lemmas: Multimap<Word, TextRange>)

class ParallelSentenceFlowManager(lifetime: Lifetime,
                                  private val sentenceProvider: SentenceProvider,
                                  loggerProvider: LoggerProvider,
                                  private val attemptsService: AttemptsService) : FlowItemProvider {
  private val attemptCategory = "ParallelSentenceWordsDE"

  val question = Property<ParallelSentenceQuestion?>(lifetime, null)
  private val logger = loggerProvider.getLogger(javaClass)

  override fun tryPresentNextItem(flowSettings: FlowSettings): Boolean {
    val pair = sentenceProvider.getSentences(Language.German, Language.English).shuffle().first()
    question.value = prepareQuestion(pair.first, pair.second)
    return true
  }

  private fun prepareQuestion(first: Sentence, second: Sentence): ParallelSentenceQuestion {
    val occurrences = LinkedHashMultimap.create<Word, TextRange>()
    for (occurrence in sentenceProvider.getOccurrences(first)) {
      val range = TextRange(occurrence.startIndex, occurrence.endIndex)
      occurrences.put(occurrence.word, range)
    }

    return ParallelSentenceQuestion(first, second, occurrences)
  }

  fun submitScore(scores: Map<Word, AttemptScore>) {
    for ((word, score) in scores) {
      attemptsService.submitAttempt(word.lemma, attemptCategory, score)
    }
  }
}