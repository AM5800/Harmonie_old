package am5800.harmonie.app.model.features.repetition

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.utils.Lifetime
import am5800.common.utils.Signal
import am5800.harmonie.app.model.services.sentencesAndLemmas.SqlSentenceAndLemmasProvider
import org.joda.time.DateTime

class LemmaAttemptResult(val lemma: Lemma, val dueDate: DateTime, val score: LearnScore)

interface LemmaRepetitionService {
  fun submitAttempt(lemma: Lemma, score: LearnScore)
  fun computeDueDate(lemma: Lemma, score: LearnScore): DateTime
  fun getNextScheduledLemma(language: Language, dateTime: DateTime): Lemma?
  fun getAttemptedLemmas(language: Language): List<Lemma>
  val attemptResultReceived: Signal<LemmaAttemptResult>

  fun countAllScheduledLemmas(language: Language, dateTime: DateTime): Int

  fun remove(lemma: Lemma)
}

class LemmaRepetitionServiceImpl(private val repetitionService: RepetitionService,
                                 lifetime: Lifetime,
                                 private val sentenceAndLemmasProvider: SqlSentenceAndLemmasProvider) : LemmaRepetitionService {
  override fun remove(lemma: Lemma) {
    val category = getCategory(lemma.language)
    repetitionService.remove(lemma.id, category)
  }

  override fun countAllScheduledLemmas(language: Language, dateTime: DateTime): Int {
    val category = getCategory(language)
    val scheduled = repetitionService.getScheduledEntities(category, dateTime)
    return scheduled.size
  }

  override val attemptResultReceived = Signal<LemmaAttemptResult>(lifetime)

  private val attemptCategory = "ParallelSentenceLemmas"
  private fun getCategory(language: Language) = attemptCategory + language.code

  override fun submitAttempt(lemma: Lemma, score: LearnScore) {
    val category = getCategory(lemma.language)
    val dueDate = repetitionService.submitAttempt(lemma.lemma, category, score)
    attemptResultReceived.fire(LemmaAttemptResult(lemma, dueDate, score))
  }

  override fun computeDueDate(lemma: Lemma, score: LearnScore): DateTime {
    val category = getCategory(lemma.language)
    return repetitionService.computeDueDate(lemma.lemma, category, score)
  }

  override fun getNextScheduledLemma(language: Language, dateTime: DateTime): Lemma? {
    val category = getCategory(language)
    val scheduled = repetitionService.getScheduledEntities(category, dateTime).firstOrNull() ?: return null
    return sentenceAndLemmasProvider.getLemmasByIds(listOf(scheduled)).single()
  }

  override fun getAttemptedLemmas(language: Language): List<Lemma> {
    val attemptedIds = repetitionService.getAttemptedItems(getCategory(language))
    return sentenceAndLemmasProvider.getLemmasByIds(attemptedIds)
  }
}