package am5800.harmonie.app.model.flow

import am5800.common.Sentence
import am5800.common.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.TextRange
import am5800.harmonie.app.model.SentenceSelector
import am5800.harmonie.app.model.SentenceSelectorResult
import am5800.harmonie.app.model.dbAccess.SentenceProvider
import am5800.harmonie.app.model.logging.LoggerProvider
import am5800.harmonie.app.model.repetition.LearnScore
import am5800.harmonie.app.model.dbAccess.WordsRepetitionService
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap


class ParallelSentenceQuestion(val question: Sentence,
                               val answer: Sentence,
                               val occurrences: Multimap<Word, TextRange>,
                               val highlightedWords: Set<Word>)

class ParallelSentenceFlowManager(lifetime: Lifetime,
                                  private val sentenceProvider: SentenceProvider,
                                  loggerProvider: LoggerProvider,
                                  private val repetitionService: WordsRepetitionService,
                                  private val sentenceSelector: SentenceSelector) : FlowItemProvider {

  val question = Property<ParallelSentenceQuestion>(lifetime, null)
  private val logger = loggerProvider.getLogger(javaClass)

  override fun tryPresentNextItem(flowSettings: FlowSettings): Boolean {
    val findResult = sentenceSelector.findBestSentence(flowSettings.questionLanguage, flowSettings.answerLanguage) ?: return false
    question.value = prepareQuestion(findResult)
    return true
  }

  private fun prepareQuestion(findResult: SentenceSelectorResult): ParallelSentenceQuestion {
    val occurrences = LinkedHashMultimap.create<Word, TextRange>()
    for (occurrence in sentenceProvider.getOccurrences(findResult.question)) {
      val range = TextRange(occurrence.startIndex, occurrence.endIndex)
      occurrences.put(occurrence.word, range)
    }

    return ParallelSentenceQuestion(findResult.question, findResult.answer, occurrences, findResult.highlightedWords)
  }

  fun submitScore(scores: Map<Word, LearnScore>) {
    for ((word, score) in scores) {
      repetitionService.submitAttempt(word, score)
    }
  }
}