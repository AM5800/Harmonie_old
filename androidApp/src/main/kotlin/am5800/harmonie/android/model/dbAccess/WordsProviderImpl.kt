package am5800.harmonie.android.model.dbAccess

import am5800.common.db.ContentDbConstants
import am5800.common.db.DbSentence
import am5800.common.db.DbWord
import am5800.common.db.SQLSentence
import am5800.harmonie.app.model.dbAccess.WordsProvider


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
    if (sentence !is SQLSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val words = ContentDbConstants.wordsTableName
    val occurrences = ContentDbConstants.wordOccurrencesTableName
    val sentenceId = sentence.id
    val query = "SELECT id, word FROM $words WHERE id IN (SELECT wordId FROM $occurrences WHERE sentenceId = $sentenceId)"

    val result = db.query2<Long, String>(query)

    return result.map { DbWord(it.first, sentence.language, it.second) }
  }
}