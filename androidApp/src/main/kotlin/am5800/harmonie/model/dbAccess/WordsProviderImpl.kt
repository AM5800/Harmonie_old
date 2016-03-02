package am5800.harmonie.model.dbAccess

import am5800.common.ContentDbConstants
import am5800.harmonie.ContentDb
import am5800.harmonie.ContentDbConsumer
import am5800.harmonie.query2


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
    val query = "SELECT key, word FROM $words WHERE key IN (SELECT wordId FROM $occurrences WHERE sentenceId = $sentenceId)"

    val result = db.query2<Long, String>(query)

    return result.map { DbWord(it.first, sentence.lang, it.second) }
  }
}