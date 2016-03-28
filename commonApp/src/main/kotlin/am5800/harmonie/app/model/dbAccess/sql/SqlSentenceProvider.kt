package am5800.harmonie.app.model.dbAccess.sql

import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence
import am5800.common.db.ContentDbConstants
import am5800.harmonie.app.model.dbAccess.SentenceProvider

class SqlSentenceProvider() : SentenceProvider, ContentDbConsumer {
  override fun getOccurrences(sentence: Sentence): List<WordOccurrence> {
    val db = database!!
    if (sentence !is SqlSentence) throw Exception("Only SqlSentences supported")

    val words = ContentDbConstants.wordsTableName
    val occurrences = ContentDbConstants.wordOccurrencesTableName

    val query = """
        SELECT $words.id, $words.lemma, $occurrences.startIndex, $occurrences.endIndex
        FROM $words
          INNER JOIN $occurrences
            ON $words.id = $occurrences.wordId
        WHERE $occurrences.sentenceId = ${sentence.id}
    """

    val wordsData = db.query4<Long, String, Int, Int>(query)

    return wordsData.map {
      WordOccurrence(SqlWord(it.value1, sentence.language, it.value2), sentence, it.value3, it.value4)
    }
  }

  var database: ContentDb? = null
  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {
  }

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  override fun getWordsInSentence(sentence: Sentence): List<Word> {
    val db = database!!
    if (sentence !is SqlSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val words = ContentDbConstants.wordsTableName
    val occurrences = ContentDbConstants.wordOccurrencesTableName
    val sentenceId = sentence.id
    val query = "SELECT id, lemma FROM $words WHERE id IN (SELECT wordId FROM $occurrences WHERE sentenceId = $sentenceId)"

    val result = db.query2<Long, String>(query)

    return result.map { SqlWord(it.first, sentence.language, it.second) }
  }
}