package am5800.harmonie.app.model.features.fillTheGap

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.functions.random
import am5800.common.utils.functions.shuffle
import am5800.common.utils.properties.NullableProperty
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.services.ContentDb
import am5800.harmonie.app.model.services.LanguageCompetenceManager
import am5800.harmonie.app.model.services.flow.FlowItemProvider
import am5800.harmonie.app.model.services.flow.FlowItemTag
import am5800.harmonie.app.model.services.query4
import am5800.harmonie.app.model.services.query5
import am5800.harmonie.app.model.services.sentencesAndWords.SqlSentenceAndWordsProvider

class FillTheGapFlowItemManagerImpl(
    private val contentDb: ContentDb,
    lifetime: Lifetime,
    private val debugOptions: DebugOptions,
    private val languageCompetenceManager: LanguageCompetenceManager,
    private val sentenceAndWordsProvider: SqlSentenceAndWordsProvider) : FlowItemProvider, FillTheGapFlowItemManager {
  override fun getAvailableDataSetSize(tag: FlowItemTag): Int {
    return 0
  }

  private val forms = getForms()
  override val supportedTags: Set<FlowItemTag> = forms.map { FillTheGapTag(it.learnLanguage) }.toSet()

  override val question = NullableProperty<FillTheGapQuestion>(lifetime)

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
        .map { FormData(it.value3, it.value5, it.value4, Language.parse(it.value2), Language.parse(it.value1)) }
  }

  private class FormData(val lemmaId: Long, val form: String, val topicId: String, val knownLanguage: Language, val learnLanguage: Language)

  override fun tryPresentNextItem(tag: FlowItemTag): Boolean {
    if (tag !is FillTheGapTag) throw UnsupportedOperationException("Category is not supported")

    val byLanguage = forms
        .filter { languageCompetenceManager.isKnown(it.knownLanguage) && it.learnLanguage == tag.learnLanguage }

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
      SELECT s1.id, s2.id, wordOccurrences.startIndex, wordOccurrences.endIndex FROM sentenceMapping
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

    val queryResult = contentDb.query4<Long, Long, Int, Int>(query).singleOrNull() ?: return null

    val sentences = sentenceAndWordsProvider.getSentencesFlat(listOf(queryResult.value1, queryResult.value2))

    val question = sentences.first()
    val answer = sentences.last()
    val variants = forms.filter { it.form != selectedForm.form && it.topicId == selectedForm.topicId }.shuffle(debugOptions.random).take(3).map { it.form }
    val occurrenceStart = queryResult.value3
    val occurrenceEnd = queryResult.value4
    return FillTheGapQuestion(question, answer, occurrenceStart, occurrenceEnd, variants, selectedForm.form)
  }
}