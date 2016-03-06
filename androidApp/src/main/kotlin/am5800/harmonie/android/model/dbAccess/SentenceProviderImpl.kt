package am5800.harmonie.android.model.dbAccess

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.db.ContentDbConstants
import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.harmonie.app.model.dbAccess.SentenceProvider
import am5800.harmonie.app.model.dbAccess.WordsProvider

class SentenceProviderImpl : SentenceProvider, ContentDbConsumer, WordsProvider {
  override fun getSentencesWithAnyOfWords(languageFrom: Language, languageTo: Language, words: List<Word>): List<Pair<Sentence, Sentence>> {
    val db = database!!
    val map = ContentDbConstants.sentenceTranslationsTableName
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

    return result.map { Pair(SqlSentence(it.value1, languageFrom, it.value2), SqlSentence(it.value3, languageTo, it.value4)) }
  }

  override fun tryFindWord(word: String, language: Language): Word? {
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

  override fun getSentences(languageFrom: Language, languageTo: Language): List<Pair<Sentence, Sentence>> {
    return getSentencesWithAnyOfWords(languageFrom, languageTo, emptyList())
  }

  override fun getWordsInSentence(sentence: Sentence): List<Word> {
    val db = database!!
    if (sentence !is SqlSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val words = ContentDbConstants.wordsTableName
    val occurrences = ContentDbConstants.wordOccurrencesTableName
    val sentenceId = sentence.id
    val query = "SELECT id, word FROM $words WHERE id IN (SELECT wordId FROM $occurrences WHERE sentenceId = $sentenceId)"

    val result = db.query2<Long, String>(query)

    return result.map { SqlWord(it.first, sentence.language, it.second) }
  }
}