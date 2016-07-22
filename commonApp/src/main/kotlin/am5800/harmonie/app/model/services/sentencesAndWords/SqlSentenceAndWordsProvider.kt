package am5800.harmonie.app.model.services.sentencesAndWords

import am5800.common.*
import am5800.harmonie.app.model.services.*
import am5800.harmonie.app.model.services.flow.LanguageCompetence

class SqlSentenceAndWordsProvider(private val contentDb: ContentDb) : SentenceAndWordsProvider {
  override fun getAllWords(learnLanguage: Language): List<WithLevel<Word>> {
    val query = """
        SELECT id, lemma, level FROM words WHERE language = '${learnLanguage.code}'
    """

    val result = contentDb.query3<Long, String, Int>(query)

    return result.map { WithLevel(SqlWord(it.value1, learnLanguage, it.value2), it.value3) }
  }

  override fun getEasiestSentencesWith(word: Word, competence: List<LanguageCompetence>, amount: Int): List<SentenceAndTranslation> {
    val wordId = (word as SqlWord).id

    // It is, of course, possible to do this in one query, but it is harder to support such queries
    val query = """
        SELECT s1.id, s2.id FROM sentences AS s1
          JOIN sentenceMapping
            ON sentenceMapping.key = s1.id
          JOIN sentences AS s2
            ON sentenceMapping.value = s2.id
          INNER JOIN wordOccurrences
            ON wordOccurrences.sentenceId = s1.id
          WHERE wordId = $wordId AND (${competenceToSql("s2.language", competence)}) AND s1.level IS NOT NULL
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
      SELECT id, uid, language, text FROM sentences
        WHERE id IN $ids
    """

    val queryResult = contentDb.query4<Long, String?, String, String>(query)

    return queryResult.map { SqlSentence(it.value1, Language.parse(it.value3), it.value4, it.value2) }
  }

  private fun competenceToSql(fieldName: String, competence: List<LanguageCompetence>): String {
    return competence.map {
      val language = it.language.code
      "($fieldName = '$language')"
    }.joinToString(" OR ")
  }

  override fun getOccurrences(sentence: Sentence): List<WordOccurrence> {
    if (sentence !is SqlSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val query = """
        SELECT words.id, words.lemma, wordOccurrences.startIndex, wordOccurrences.endIndex, words.language
        FROM words
          INNER JOIN wordOccurrences
            ON words.id = wordOccurrences.wordId
        WHERE wordOccurrences.sentenceId = ${sentence.sqlId}
    """

    val wordsData = contentDb.query5<Long, String, Int, Int, String>(query)

    val result = wordsData.map {
      WordOccurrence(SqlWord(it.value1, Language.parse(it.value5), it.value2), sentence, it.value3, it.value4)
    }

    if (result.isNotEmpty() && result.any { it.word.language != sentence.language }) throw Exception("Word language differ from sentence language")
    return result
  }

  override fun getWordsInSentence(sentence: Sentence): List<Word> {
    if (sentence !is SqlSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val sentenceId = sentence.sqlId
    val query = "SELECT id, lemma FROM words WHERE id IN (SELECT wordId FROM wordOccurrences WHERE sentenceId = $sentenceId)"

    val result = contentDb.query2<Long, String>(query)

    return result.map { SqlWord(it.first, sentence.language, it.second) }
  }

}