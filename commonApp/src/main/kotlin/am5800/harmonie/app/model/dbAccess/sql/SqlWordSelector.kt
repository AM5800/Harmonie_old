package am5800.harmonie.app.model.dbAccess.sql

import am5800.common.Language
import am5800.common.code
import am5800.common.db.ContentDbConstants
import am5800.common.db.Word
import am5800.harmonie.app.model.WordSelector

class SqlWordSelector : WordSelector, ContentDbConsumer {
  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {

  }

  private var database: ContentDb? = null

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  override fun findBestWord(exclude: List<Word>, language: Language): Word? {
    val counts = ContentDbConstants.wordCountsTableName
    val words = ContentDbConstants.wordsTableName
    val lang = language.code()
    val ids = exclude.filterIsInstance<SqlWord>().map { it.id }.joinToString(", ")
    val query = """
      SELECT $words.id, $words.lemma
        FROM $words
        INNER JOIN $counts
          ON $counts.wordId = $words.id
        WHERE $words.language='$lang' AND $words.id NOT IN ($ids)
        ORDER BY $counts.count DESC
        LIMIT 1
    """
    return database!!.query2<Long, String>(query).map { SqlWord(it.first, language, it.second) }.singleOrNull()
  }

}