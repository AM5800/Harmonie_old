package am5800.harmonie.android.model.dbAccess

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.db.ContentDbConstants
import am5800.common.db.DbSentence
import am5800.common.db.SQLSentence
import am5800.harmonie.app.model.dbAccess.SentenceProvider

class SentenceProviderImpl : SentenceProvider, ContentDbConsumer {
  var database: ContentDb? = null
  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {
  }

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  override fun getSentences(languageFrom: Language, languageTo: Language): List<Pair<DbSentence, DbSentence>> {
    val db = database!!
    val map = ContentDbConstants.sentenceMappingTableName
    val sentences = ContentDbConstants.sentencesTableName
    val langFrom = LanguageParser.toShortString(languageFrom)
    val langTo = LanguageParser.toShortString(languageTo)

    val query = """
        SELECT s1.id, s1.text, s2.id, s2.text
        FROM $map
          INNER JOIN $sentences AS s1
            ON $map.key = s1.id
          INNER JOIN $sentences AS s2
            ON $map.value = s2.id
        WHERE s1.lang='$langFrom' AND s2.lang='$langTo'"""

    val result = db.query4<Long, String, Long, String>(query)

    return result.map { Pair(SQLSentence(it.value1, languageFrom, it.value2), SQLSentence(it.value3, languageTo, it.value4)) }
  }
}