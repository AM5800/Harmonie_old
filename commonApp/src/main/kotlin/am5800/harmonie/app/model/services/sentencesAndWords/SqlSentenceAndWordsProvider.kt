package am5800.harmonie.app.model.services.sentencesAndWords

import am5800.common.*
import am5800.common.utils.functions.random
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.services.*
import am5800.harmonie.app.model.services.flow.LanguageCompetence

class SqlSentenceAndWordsProvider(private val contentDb: ContentDb,
                                  private val debugOptions: DebugOptions) : SentenceAndWordsProvider {
  override fun getAllWords(learnLanguage: Language): List<WithLevel<Word>> {
    val query = """
        SELECT id, lemma, level FROM words WHERE language = '${learnLanguage.code}'
    """

    val result = contentDb.query3<Long, String, Int>(query)

    return result.map { WithLevel(SqlWord(it.value1, learnLanguage, it.value2), it.value3) }
  }

  override fun getEasiestRandomSentenceWith(word: Word, competence: List<LanguageCompetence>): SentenceAndTranslation? {
    val wordId = (word as SqlWord).id
    val learnLanguage = word.language

    val query = """
        SELECT s1.id, s1.text, s2.id, s2.text, s2.language, s1.level FROM sentences AS s1
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

    val queryResult = contentDb.query6<Long, String, Long, String, String, Int>(query)
    if (queryResult.isEmpty()) return null

    val easiest = queryResult.filter { it.value6 == queryResult.first().value6 }
    val result = easiest.random(debugOptions.random)

    val learnLanguageSentence = SqlSentence(result.value1, learnLanguage, result.value2)
    val knownLanguageSentence = SqlSentence(result.value3, LanguageParser.parse(result.value5), result.value4)
    return SentenceAndTranslation(learnLanguageSentence, knownLanguageSentence)
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
        WHERE wordOccurrences.sentenceId = ${sentence.id}
    """

    val wordsData = contentDb.query5<Long, String, Int, Int, String>(query)

    val result = wordsData.map {
      WordOccurrence(SqlWord(it.value1, LanguageParser.Companion.parse(it.value5), it.value2), sentence, it.value3, it.value4)
    }

    if (result.isNotEmpty() && result.any { it.word.language != sentence.language }) throw Exception("Word language differ from sentence language")
    return result
  }

  override fun getWordsInSentence(sentence: Sentence): List<Word> {
    if (sentence !is SqlSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val sentenceId = sentence.id
    val query = "SELECT id, lemma FROM words WHERE id IN (SELECT wordId FROM wordOccurrences WHERE sentenceId = $sentenceId)"

    val result = contentDb.query2<Long, String>(query)

    return result.map { SqlWord(it.first, sentence.language, it.second) }
  }

}