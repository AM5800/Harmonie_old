package am5800.harmonie.app.model.features.parallelSentence

import am5800.common.Sentence
import am5800.common.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.TextRange
import am5800.common.utils.properties.NullableProperty
import am5800.harmonie.app.model.features.repetition.LearnScore
import am5800.harmonie.app.model.features.repetition.WordsRepetitionService
import am5800.harmonie.app.model.services.LanguageCompetenceManager
import am5800.harmonie.app.model.services.flow.FlowItemProvider
import am5800.harmonie.app.model.services.flow.FlowItemTag
import am5800.harmonie.app.model.services.languagePairs.LanguagePairsProvider
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndTranslation
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndWordsProvider
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap


class ParallelSentenceQuestion(val question: Sentence,
                               val answer: Sentence,
                               val occurrences: Multimap<Word, TextRange>)

class ParallelSentenceFlowManager(lifetime: Lifetime,
                                  private val sentenceProvider: SentenceAndWordsProvider,
                                  private val repetitionService: WordsRepetitionService,
                                  private val sentenceSelector: ParallelSentenceSelector,
                                  languagePairsProvider: LanguagePairsProvider,
                                  private val languageCompetenceManager: LanguageCompetenceManager) : FlowItemProvider {
  private val availableLanguagePairs = languagePairsProvider.getAvailableLanguagePairs()

  override fun getAvailableDataSetSize(tag: FlowItemTag): Int {
    if (tag !is ParallelSentenceTag) return 0
    val pair = availableLanguagePairs.single { it.entity.learnLanguage == tag.learnLanguage && languageCompetenceManager.isKnown(it.entity.knownLanguage) }
    return pair.count
  }

  override val supportedTags = availableLanguagePairs.map { ParallelSentenceTag(it.entity.learnLanguage) }.toSet()

  val question = NullableProperty<ParallelSentenceQuestion>(lifetime, null)

  override fun tryPresentNextItem(tag: FlowItemTag): Boolean {
    if (tag !is ParallelSentenceTag) throw UnsupportedOperationException("Category is not supported")
    val findResult = sentenceSelector.selectSentenceToShow(tag.learnLanguage, languageCompetenceManager.languageCompetence)
    if (findResult == null) {
      question.value = null
      return false
    }
    question.value = prepareQuestion(findResult)
    return true
  }

  private fun prepareQuestion(findResult: SentenceAndTranslation): ParallelSentenceQuestion {
    val occurrences = LinkedHashMultimap.create<Word, TextRange>()
    for (occurrence in sentenceProvider.getOccurrences(findResult.sentence)) {
      val range = TextRange(occurrence.startIndex, occurrence.endIndex)
      occurrences.put(occurrence.word, range)
    }

    return ParallelSentenceQuestion(findResult.sentence, findResult.translation, occurrences)
  }

  fun submitScore(scores: Map<Word, LearnScore>, sentenceScore: SentenceScore) {
    sentenceSelector.submitScore(question.value!!.question, sentenceScore)
    for ((word, score) in scores) {
      repetitionService.submitAttempt(word, score)
    }
  }
}