package am5800.harmonie.model.dbAccess

import am5800.common.ContentDbConstants
import am5800.harmonie.ContentDb
import am5800.harmonie.ContentDbConsumer
import am5800.harmonie.query3


class WordsProviderImpl : WordsProvider, ContentDbConsumer {
  private var database: ContentDb? = null
  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {
  }

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  override fun getWordsInSentence(sentence: DbSentence): List<DbWord> {
    val db = database!!
    val words = ContentDbConstants.wordsTableName
    val occurrences = ContentDbConstants.wordOccurrencesTableName
    val sentenceId = sentence.id
    val query = "SELECT key, word, frequency FROM $words WHERE key IN (SELECT wordId FROM $occurrences WHERE sentenceId = $sentenceId)"

    val result = db.query3<Long, String, Double>(query)

    return result.map { DbWord(it.value1, sentence.lang, it.value2, it.value3) }
  }
}