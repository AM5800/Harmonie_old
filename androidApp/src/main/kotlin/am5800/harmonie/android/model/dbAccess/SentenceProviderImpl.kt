package am5800.harmonie.android.model.dbAccess

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.db.*
import am5800.harmonie.app.model.dbAccess.SentenceProvider
import am5800.harmonie.app.model.dbAccess.WordsProvider

class SentenceProviderImpl : SentenceProvider, ContentDbConsumer, WordsProvider {
  override fun getSentencesWithAnyOfWords(languageFrom: Language, languageTo: Language, words: List<DbWord>): List<Pair<DbSentence, DbSentence>> {
    val db = database!!
    val map = ContentDbConstants.sentenceMappingTableName
    val sentences = ContentDbConstants.sentencesTableName
    val langFrom = LanguageParser.toShortString(languageFrom)
    val langTo = LanguageParser.toShortString(languageTo)

    var query = """
        SELECT s1.id, s1.text, s2.id, s2.text
        FROM $map
          INNER JOIN $sentences AS s1
            ON $map.key = s1.id
          INNER JOIN $sentences AS s2
            ON $map.value = s2.id
        WHERE s1.lang='$langFrom' AND s2.lang='$langTo'"""

    val sqlWords = words.filterIsInstance<SqlWord>()
    if (sqlWords.any()) {
      val ids = sqlWords.map { it.id }.joinToString(", ")
      val occurrences = ContentDbConstants.wordOccurrencesTableName
      query += " AND s1.id IN (SELECT sentenceId FROM $occurrences WHERE wordId IN ($ids))"
    }

    val result = db.query4<Long, String, Long, String>(query)

    return result.map { Pair(SQLSentence(it.value1, languageFrom, it.value2), SQLSentence(it.value3, languageTo, it.value4)) }
  }

  override fun tryFindWord(word: String, language: Language): DbWord? {
    val w = word.toLowerCase().trim()

    val words = ContentDbConstants.wordsTableName
    val lang = LanguageParser.toShortString(language)
    val query = "SELECT id FROM $words WHERE word = '$w' AND lang = '$lang'"
    val id = database!!.query1<Long>(query).singleOrNull() ?: return null

    return SqlWord(id, language, word)
  }

  var database: ContentDb? = null
  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {
  }

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  override fun getSentences(languageFrom: Language, languageTo: Language): List<Pair<DbSentence, DbSentence>> {
    return getSentencesWithAnyOfWords(languageFrom, languageTo, emptyList())
  }

  override fun getWordsInSentence(sentence: DbSentence): List<DbWord> {
    val db = database!!
    if (sentence !is SQLSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val words = ContentDbConstants.wordsTableName
    val occurrences = ContentDbConstants.wordOccurrencesTableName
    val sentenceId = sentence.id
    val query = "SELECT id, word FROM $words WHERE id IN (SELECT wordId FROM $occurrences WHERE sentenceId = $sentenceId)"

    val result = db.query2<Long, String>(query)

    return result.map { SqlWord(it.first, sentence.language, it.second) }
  }
}