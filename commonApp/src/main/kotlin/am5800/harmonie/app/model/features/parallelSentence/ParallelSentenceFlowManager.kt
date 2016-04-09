package am5800.harmonie.app.model.features.parallelSentence

import am5800.common.Sentence
import am5800.common.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.TextRange
import am5800.harmonie.app.model.features.flow.FlowItemCategory
import am5800.harmonie.app.model.features.flow.FlowItemProvider
import am5800.harmonie.app.model.features.repetition.LearnScore
import am5800.harmonie.app.model.features.repetition.WordsRepetitionService
import am5800.harmonie.app.model.services.PreferredLanguagesService
import am5800.harmonie.app.model.services.SentenceProvider
import am5800.harmonie.app.model.services.SentenceSelector
import am5800.harmonie.app.model.services.SentenceSelectorResult
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap


class ParallelSentenceQuestion(val question: Sentence,
                               val answer: Sentence,
                               val occurrences: Multimap<Word, TextRange>,
                               val highlightedWords: Set<Word>)

class ParallelSentenceFlowManager(lifetime: Lifetime,
                                  private val preferredLanguagesService: PreferredLanguagesService,
                                  private val sentenceProvider: SentenceProvider,
                                  private val repetitionService: WordsRepetitionService,
                                  private val sentenceSelector: SentenceSelector) : FlowItemProvider {
  override val supportedCategories: Set<FlowItemCategory>
    get() = preferredLanguagesService.learnLanguages.value!!.map { ParallelSentenceCategory(it) }.toSet()

  val question = Property<ParallelSentenceQuestion>(lifetime, null)

  override fun tryPresentNextItem(category: FlowItemCategory): Boolean {
    if (category !is ParallelSentenceCategory) throw UnsupportedOperationException("Category is not supported")
    val findResult = sentenceSelector.findBestSentenceByAttempts(category.questionLanguage, preferredLanguagesService.knownLanguages.value!!) ?: return false
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