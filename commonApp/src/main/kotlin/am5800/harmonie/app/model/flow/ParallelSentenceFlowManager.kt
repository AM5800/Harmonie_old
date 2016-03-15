package am5800.harmonie.app.model.flow

import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.TextRange
import am5800.harmonie.app.model.dbAccess.AttemptScore
import am5800.harmonie.app.model.dbAccess.SentenceProvider
import am5800.harmonie.app.model.dbAccess.SentenceSelector
import am5800.harmonie.app.model.dbAccess.WordsRepetitionService
import am5800.harmonie.app.model.logging.LoggerProvider
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap


class ParallelSentenceQuestion(val question: Sentence, val answer: Sentence, val lemmas: Multimap<Word, TextRange>)

class ParallelSentenceFlowManager(lifetime: Lifetime,
                                  private val sentenceProvider: SentenceProvider,
                                  loggerProvider: LoggerProvider,
                                  private val repetitionService: WordsRepetitionService,
                                  private val sentenceSelector: SentenceSelector) : FlowItemProvider {

  val question = Property<ParallelSentenceQuestion?>(lifetime, null)
  private val logger = loggerProvider.getLogger(javaClass)

  override fun tryPresentNextItem(flowSettings: FlowSettings): Boolean {
    val pair = sentenceSelector.findBestSentence(flowSettings.questionLanguage, flowSettings.answerLanguage) ?: return false
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
      val dueDate = repetitionService.submitAttempt(word, score)
      logger.info("'${word.lemma}' has score of $score and scheduled to ${dueDate.toString()} ")
    }
  }

}