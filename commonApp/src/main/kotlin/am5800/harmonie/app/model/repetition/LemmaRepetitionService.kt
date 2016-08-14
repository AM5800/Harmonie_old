package am5800.harmonie.app.model.repetition

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.utils.Lifetime
import am5800.common.utils.Signal
import am5800.harmonie.app.model.sentencesAndLemmas.SqlSentenceAndLemmasProvider
import org.joda.time.DateTime

class LemmaAttemptResult(val lemma: Lemma, val dueDate: DateTime, val score: LearnScore)

interface LemmaRepetitionService {
  fun submitAttempt(lemma: Lemma, score: LearnScore)
  fun getNextScheduledLemma(language: Language, dateTime: DateTime): Lemma?
  fun getAttemptedLemmas(language: Language): List<Lemma>
  val attemptResultReceived: Signal<LemmaAttemptResult>
  fun countOnDueLemmas(language: Language, dateTime: DateTime): Int
  fun getDueDates(lemmas: List<Lemma>): List<Pair<Lemma, DateTime?>>
}

class LemmaRepetitionServiceImpl(private val repetitionService: RepetitionService,
                                 lifetime: Lifetime,
                                 private val sentenceAndLemmasProvider: SqlSentenceAndLemmasProvider) : LemmaRepetitionService {
  override fun getDueDates(lemmas: List<Lemma>): List<Pair<Lemma, DateTime?>> {
    if (lemmas.isEmpty()) return emptyList()
    val language = lemmas.first().language
    assert(lemmas.all { it.language == language })
    val category = getCategory(language)
    val dueDates = repetitionService.getDueDates(lemmas.map { it.id }, category)

    return lemmas.map { Pair(it, dueDates[it.id]) }
  }

  override fun countOnDueLemmas(language: Language, dateTime: DateTime): Int {
    val category = getCategory(language)
    return repetitionService.countOnDueItems(category, dateTime)
  }

  override val attemptResultReceived = Signal<LemmaAttemptResult>(lifetime)

  private val attemptCategory = "ParallelSentenceLemmas"
  private fun getCategory(language: Language) = attemptCategory + language.code

  override fun submitAttempt(lemma: Lemma, score: LearnScore) {
    val category = getCategory(lemma.language)
    val dueDate = repetitionService.submitAttempt(lemma.id, category, score)
    attemptResultReceived.fire(LemmaAttemptResult(lemma, dueDate, score))
  }

  override fun getNextScheduledLemma(language: Language, dateTime: DateTime): Lemma? {
    val category = getCategory(language)
    var scheduled = repetitionService.getNextScheduledEntity(category, dateTime) ?: return null
    return sentenceAndLemmasProvider.getLemmasByIds(listOf(scheduled)).single()
  }

  override fun getAttemptedLemmas(language: Language): List<Lemma> {
    val attemptedIds = repetitionService.getAttemptedItems(getCategory(language))
    return sentenceAndLemmasProvider.getLemmasByIds(attemptedIds)
  }
}