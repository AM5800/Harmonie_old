package am5800.harmonie.app.model.services.sentencesAndLemmas

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.LemmaOccurrence
import am5800.common.Sentence
import am5800.harmonie.app.model.services.*
import am5800.harmonie.app.model.services.flow.LanguageCompetence

class SqlSentenceAndLemmasProvider(private val contentDb: ContentDb) : SentenceAndLemmasProvider {
  override fun getLemmasByIds(lemmaId: List<String>): List<Lemma> {
    val ids = lemmaId.joinToString(", ") { "'$it'" }
    val query = """
      SELECT id, lemmaId, level FROM lemmas WHERE lemmaId IN ($ids)
    """

    val queryResult = contentDb.query3<Long, String, Int>(query)

    return queryResult.map { SqlLemma(it.value1, it.value2, it.value3) }


  }

  override fun getAllLemmas(learnLanguage: Language): List<Lemma> {
    val query = """
        SELECT id, lemmaId, level FROM lemmas WHERE language = '${learnLanguage.code}'
    """
    val result = contentDb.query3<Long, String, Int>(query)
    return result.map { SqlLemma(it.value1, it.value2, it.value3) }
  }

  override fun getEasiestSentencesWith(lemma: Lemma, competence: List<LanguageCompetence>, amount: Int): List<SentenceAndTranslation> {
    val lemmaId = (lemma as SqlLemma).id

    // It is, of course, possible to do this in one query, but it is harder to support such queries
    val query = """
        SELECT s1.id, s2.id FROM sentences AS s1
          JOIN sentenceMapping
            ON sentenceMapping.key = s1.id
          JOIN sentences AS s2
            ON sentenceMapping.value = s2.id
          INNER JOIN lemmaOccurrences
            ON lemmaOccurrences.sentenceId = s1.id
          INNER JOIN lemmas
            ON lemmas.id = lemmaOccurrences.lemmaId
          WHERE lemmas.lemmaId='$lemmaId'
            AND (${competenceToSql("s2.language", competence)})
            AND s1.level IS NOT NULL
          ORDER BY s1.level
          LIMIT 100
    """

    val queryResult = contentDb.query2<Long, Long>(query)

    val sentences = getSentences(queryResult.map { listOf(it.first, it.second) })
        .map { SentenceAndTranslation(it.first(), it.last()) }

    return sentences
  }

  fun getSentences(sqlIds: List<List<Long>>): List<List<SqlSentence>> {
    val unfolded = sqlIds.flatMap { it }
    val sentences = getSentencesFlat(unfolded).map { Pair(it.sqlId, it) }.toMap()

    val result = sqlIds.map { group ->
      group.map {
        sentences[it]!!
      }
    }

    return result
  }

  fun getSentencesFlat(sqlIds: List<Long>): List<SqlSentence> {
    val ids = "(${sqlIds.joinToString(", ") { it.toString() }})"
    val query = """
      SELECT id, uid, language, text, level FROM sentences
        WHERE id IN $ids
    """

    val queryResult = contentDb.query5<Long, String, String, String, Int?>(query)

    return queryResult.map { SqlSentence(it.value1, Language.parse(it.value3), it.value4, it.value2, it.value5) }
  }

  private fun competenceToSql(fieldName: String, competence: List<LanguageCompetence>): String {
    return competence.map {
      val language = it.language.code
      "($fieldName = '$language')"
    }.joinToString(" OR ")
  }

  override fun getOccurrences(sentence: Sentence): List<LemmaOccurrence> {
    if (sentence !is SqlSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val query = """
        SELECT lemmas.lemmaId, lemmaOccurrences.startIndex, lemmaOccurrences.endIndex
        FROM lemmas
          INNER JOIN lemmaOccurrences
            ON lemmas.id = lemmaOccurrences.lemmaId
        WHERE lemmaOccurrences.sentenceId = ${sentence.sqlId}
    """

    val queryResult = contentDb.query3<String, Int, Int>(query)
    val lemmas = getLemmasByIds(queryResult.map { it.value1 }).map { Pair(it.id, it) }.toMap()

    val result = queryResult.map {
      LemmaOccurrence(lemmas[it.value1]!!, sentence, it.value2, it.value3)
    }

    if (result.isNotEmpty() && result.any { it.lemma.language != sentence.language }) throw Exception("Lemma language differ from sentence language")
    return result
  }
}