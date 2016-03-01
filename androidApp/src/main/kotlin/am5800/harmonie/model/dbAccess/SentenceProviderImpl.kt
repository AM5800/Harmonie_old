package am5800.harmonie.model.dbAccess

import am5800.common.ContentDbConstants
import am5800.common.Language
import am5800.common.LanguageParser
import am5800.harmonie.ContentDb
import am5800.harmonie.ContentDbConsumer

class SentenceProviderImpl : SentenceProvider, ContentDbConsumer {
  var database: ContentDb? = null
  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {
  }

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  override fun getSentences(languageFrom: Language, languageTo: Language): List<Pair<String, String>> {
    val db = database!!
    val map = ContentDbConstants.sentenceMappingTableName
    val sentences = ContentDbConstants.sentencesTableName
    val langFrom = LanguageParser.toShort(languageFrom)
    val langTo = LanguageParser.toShort(languageTo)

    val query = """
        SELECT s1.text, s2.text
        FROM $map
          INNER JOIN $sentences AS s1
            ON $map.key = s1.id
          INNER JOIN $sentences AS s2
            ON $map.value = s2.id
        WHERE s1.lang='$langFrom' AND s2.lang='$langTo'"""

    val cursor = db.rawQuery(query, emptyArray())

    val result = mutableListOf<Pair<String, String>>()

    while (cursor.moveToNext()) {
      val s1 = cursor.getString(0)
      val s2 = cursor.getString(1)
      result.add(Pair(s1, s2))
    }


    return result
  }
}