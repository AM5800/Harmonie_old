package am5800.harmonie.app.model.flow.germanExercises

import am5800.common.*
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.functions.shuffle
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.dbAccess.PreferredLanguagesService
import am5800.harmonie.app.model.dbAccess.sql.ContentDb
import am5800.harmonie.app.model.dbAccess.sql.formatLanguageCondition
import am5800.harmonie.app.model.dbAccess.sql.query1
import am5800.harmonie.app.model.dbAccess.sql.query5
import am5800.harmonie.app.model.flow.FlowItemCategory
import am5800.harmonie.app.model.flow.FlowItemProvider


class GermanSeinFormQuestion(val question: Sentence, val answer: Sentence, val occurrence: WordOccurrence, wrongVariants: List<String>)

class GermanSeinFormFlowItemManager(
    private val preferredLanguagesService: PreferredLanguagesService,
    private val contentDb: ContentDb,
    lifetime: Lifetime,
    private val debugOptions: DebugOptions) : FlowItemProvider {
  override val supportedCategories: Set<FlowItemCategory> = setOf(SeinFormQuizCategory(Language.German))

  val question = Property<GermanSeinFormQuestion>(lifetime, null)
  private val seinWordId = contentDb.query1<Long>("SELECT id FROM words WHERE language='${Language.German.code}' AND lemma='sein'").single()
  private val forms = getForms(seinWordId)

  private fun getForms(wordId: Long): List<String> {
    val query = """
      SELECT DISTINCT(form) FROM specialFormOccurrences
        INNER JOIN wordOccurrences
          ON specialFormOccurrences.occurrenceId = wordOccurrences.id
        WHERE wordId = $wordId
    """
    return contentDb.query1<String>(query)
  }

  override fun tryPresentNextItem(category: FlowItemCategory): Boolean {
    if (category !is SeinFormQuizCategory) throw UnsupportedOperationException("Category is not supported")

    val knownLanguages = preferredLanguagesService.knownLanguages.value!!
    val selectedForm = forms.shuffle(debugOptions.randomSeed).first()

    val q = getQuestion(selectedForm, knownLanguages) ?: return false
    question.value = q
    return true
  }

  private fun getQuestion(selectedForm: String, knownLanguages: List<Language>): GermanSeinFormQuestion? {
    val langs = formatLanguageCondition("s2.language", knownLanguages)
    val query = """
      SELECT s1.text, s2.text, s2.language, wordOccurrences.startIndex, wordOccurrences.endIndex FROM sentenceMapping
        INNER JOIN sentences AS s1
          ON s1.id = sentenceMapping.key
        INNER JOIN sentences AS s2
          ON s2.id = sentenceMapping.value
        INNER JOIN wordOccurrences
          ON wordOccurrences.sentenceId = s1.id
        INNER JOIN specialFormOccurrences
          ON specialFormOccurrences.occurrenceId = wordOccurrences.id
        WHERE wordOccurrences.wordId=$seinWordId AND specialFormOccurrences.form='$selectedForm' AND $langs AND s1.language='${Language.German.code}'
        ORDER BY RANDOM()
        LIMIT 1
    """

    val queryResult = contentDb.query5<String, String, String, Long, Long>(query).singleOrNull() ?: return null

    val question = Sentence(Language.German, queryResult.value1)
    val answer = Sentence(LanguageParser.parse(queryResult.value3), queryResult.value2)
    val occurrence = WordOccurrence(Word(Language.German, "sein"), question, queryResult.value4.toInt(), queryResult.value5.toInt())
    val variants = forms.filterNot { it == selectedForm }.shuffle(debugOptions.randomSeed).take(3)
    return GermanSeinFormQuestion(question, answer, occurrence, variants)
  }
}