package am5800.harmonie.app.model.parallelSentence

import am5800.common.Lemma
import am5800.common.Sentence
import am5800.common.utils.Lifetime
import am5800.common.utils.TextRange
import am5800.common.utils.properties.NullableProperty
import am5800.harmonie.app.model.LanguageCompetenceManager
import am5800.harmonie.app.model.flow.FlowItemProvider
import am5800.harmonie.app.model.flow.FlowItemTag
import am5800.harmonie.app.model.repetition.LearnScore
import am5800.harmonie.app.model.repetition.LemmaRepetitionService
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndTranslation
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap


class ParallelSentenceQuestion(val question: Sentence,
                               val answer: Sentence,
                               val occurrences: Multimap<Lemma, TextRange>)

class ParallelSentenceFlowManager(lifetime: Lifetime,
                                  private val sentenceProvider: SentenceAndLemmasProvider,
                                  private val repetitionService: LemmaRepetitionService,
                                  private val sentenceSelector: ParallelSentenceSelector,
                                  private val languageCompetenceManager: LanguageCompetenceManager) : FlowItemProvider {

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
    val occurrences = LinkedHashMultimap.create<Lemma, TextRange>()
    for (occurrence in sentenceProvider.getOccurrences(findResult.sentence)) {
      val range = TextRange(occurrence.startIndex, occurrence.endIndex)
      occurrences.put(occurrence.lemma, range)
    }

    return ParallelSentenceQuestion(findResult.sentence, findResult.translation, occurrences)
  }

  fun submitScore(scores: Map<Lemma, LearnScore>, sentenceScore: SentenceScore) {
    sentenceSelector.submitScore(question.value!!.question, sentenceScore)
    for ((lemma, score) in scores) {
      repetitionService.submitAttempt(lemma, score)
    }
  }
}