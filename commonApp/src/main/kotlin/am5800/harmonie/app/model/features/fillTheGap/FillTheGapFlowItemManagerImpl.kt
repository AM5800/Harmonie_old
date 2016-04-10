package am5800.harmonie.app.model.features.fillTheGap

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.Sentence
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.functions.random
import am5800.common.utils.functions.shuffle
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.flow.FlowItemCategory
import am5800.harmonie.app.model.features.flow.FlowItemProvider
import am5800.harmonie.app.model.services.ContentDb
import am5800.harmonie.app.model.services.query5

class FillTheGapFlowItemManagerImpl(
    private val contentDb: ContentDb,
    lifetime: Lifetime,
    private val debugOptions: DebugOptions) : FlowItemProvider, FillTheGapFlowItemManager {
  private val forms = getForms()
  override val supportedCategories: Set<FlowItemCategory> = forms.map { FillTheGapCategory(it.learnLanguage, it.knownLanguage) }.toSet()

  override val question = Property<FillTheGapQuestion>(lifetime, null)

  private fun getForms(): List<FormData> {
    val query = """
      SELECT s1.language, s2.language, wordId, topic, form FROM fillTheGapOccurrences
        INNER JOIN wordOccurrences
          ON wordOccurrences.id=fillTheGapOccurrences.occurrenceId
        INNER JOIN sentences AS s1
          ON s1.id = wordOccurrences.sentenceId
        INNER JOIN sentenceMapping
          ON sentenceMapping.key = s1.id
        INNER JOIN sentences AS s2
          ON sentenceMapping.value = s2.id
        GROUP BY wordId, form
    """
    return contentDb.query5<String, String, Long, String, String>(query)
        .map { FormData(it.value3, it.value5, it.value4, LanguageParser.parse(it.value2), LanguageParser.parse(it.value1)) }
  }

  private class FormData(val lemmaId: Long, val form: String, val topicId: String, val knownLanguage: Language, val learnLanguage: Language)

  override fun tryPresentNextItem(category: FlowItemCategory): Boolean {
    if (category !is FillTheGapCategory) throw UnsupportedOperationException("Category is not supported")

    val byLanguage = forms
        .filter { it.knownLanguage == category.knownLanguage && it.learnLanguage == category.learnLanguage }

    val byTopic = byLanguage
        .groupBy { it.topicId }.toList()
        .random(debugOptions.random)
        .second

    val selectedForm: FormData = byTopic.random(debugOptions.random)

    val q = getQuestion(selectedForm) ?: return false
    question.value = q
    return true
  }

  private fun getQuestion(selectedForm: FormData): FillTheGapQuestion? {
    val query = """
      SELECT s1.text, s2.text, s2.language, wordOccurrences.startIndex, wordOccurrences.endIndex FROM sentenceMapping
        INNER JOIN sentences AS s1
          ON s1.id = sentenceMapping.key
        INNER JOIN sentences AS s2
          ON s2.id = sentenceMapping.value
        INNER JOIN wordOccurrences
          ON wordOccurrences.sentenceId = s1.id
        INNER JOIN fillTheGapOccurrences
          ON fillTheGapOccurrences.occurrenceId = wordOccurrences.id
        WHERE wordOccurrences.wordId=${selectedForm.lemmaId} AND fillTheGapOccurrences.form='${selectedForm.form}'
          AND s2.language='${selectedForm.knownLanguage.code}' AND s1.language='${selectedForm.learnLanguage.code}'
        ORDER BY RANDOM()
        LIMIT 1
    """

    val queryResult = contentDb.query5<String, String, String, Long, Long>(query).singleOrNull() ?: return null

    val question = Sentence(Language.German, queryResult.value1)
    val answer = Sentence(LanguageParser.parse(queryResult.value3), queryResult.value2)
    val variants = forms.filter { it.form != selectedForm.form && it.topicId == selectedForm.topicId }.shuffle(debugOptions.random).take(3).map { it.form }
    return FillTheGapQuestion(question, answer, queryResult.value4.toInt(), queryResult.value5.toInt(), variants, selectedForm.form)
  }
}