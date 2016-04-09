package am5800.harmonie.app.model.features.fillTheGap.german

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.Sentence
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.functions.random
import am5800.common.utils.functions.shuffle
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.fillTheGap.FillTheGapInParallelSentenceFlowItemManager
import am5800.harmonie.app.model.features.fillTheGap.FillTheGapInParallelSentenceQuestion
import am5800.harmonie.app.model.features.flow.FlowItemCategory
import am5800.harmonie.app.model.features.flow.FlowItemProvider
import am5800.harmonie.app.model.services.ContentDb
import am5800.harmonie.app.model.services.PreferredLanguagesService
import am5800.harmonie.app.model.services.impl.formatLanguageCondition
import am5800.harmonie.app.model.services.query3
import am5800.harmonie.app.model.services.query5

class GermanExerciseFlowItemManager(
    private val preferredLanguagesService: PreferredLanguagesService,
    private val contentDb: ContentDb,
    lifetime: Lifetime,
    private val debugOptions: DebugOptions) : FlowItemProvider, FillTheGapInParallelSentenceFlowItemManager {
  override val supportedCategories: Set<FlowItemCategory> = computeSupportedCategories()

  private fun computeSupportedCategories(): Set<FlowItemCategory> {
    if (preferredLanguagesService.learnLanguages.value?.contains(Language.German) == true) return setOf(GermanExerciseCategory())
    return emptySet()
  }

  override val question = Property<FillTheGapInParallelSentenceQuestion>(lifetime, null)
  private val forms = getForms()

  private class FormData(val lemmaId: Long, val form: String, val topicId: String)

  private fun getForms(): List<FormData> {
    val query = """
      SELECT wordOccurrences.wordId, lemma, specialFormOccurrences.form FROM words
        INNER JOIN wordOccurrences
          ON words.id = wordOccurrences.wordId
        INNER JOIN specialFormOccurrences
          ON specialFormOccurrences.occurrenceId = wordOccurrences.id
        WHERE language='${Language.German.code}'
        GROUP BY lemma, form
    """

    return contentDb.query3<Long, String, String>(query).map {
      // TODO: merge topics (die, ein, eine)
      FormData(it.value1, it.value3, it.value2)
    }
  }

  override fun tryPresentNextItem(category: FlowItemCategory): Boolean {
    if (category !is GermanExerciseCategory) throw UnsupportedOperationException("Category is not supported")

    val knownLanguages = preferredLanguagesService.knownLanguages.value!!
    val selectedForm = forms.random(debugOptions.randomSeed)

    val q = getQuestion(selectedForm, knownLanguages) ?: return false
    question.value = q
    return true
  }

  private fun getQuestion(selectedForm: FormData, knownLanguages: List<Language>): FillTheGapInParallelSentenceQuestion? {
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
        WHERE wordOccurrences.wordId=${selectedForm.lemmaId} AND specialFormOccurrences.form='${selectedForm.form}' AND $langs AND s1.language='${Language.German.code}'
        ORDER BY RANDOM()
        LIMIT 1
    """

    val queryResult = contentDb.query5<String, String, String, Long, Long>(query).singleOrNull() ?: return null

    val question = Sentence(Language.German, queryResult.value1)
    val answer = Sentence(LanguageParser.Companion.parse(queryResult.value3), queryResult.value2)
    val variants = forms.filter { it != selectedForm && it.topicId == selectedForm.topicId }.shuffle(debugOptions.randomSeed).take(3).map { it.form }
    return FillTheGapInParallelSentenceQuestion(question, answer, queryResult.value4.toInt(), queryResult.value5.toInt(), variants, selectedForm.form)
  }
}