package am5800.harmonie.app.model.lemmasMeaning

import am5800.common.Language
import am5800.common.Lemma
import am5800.harmonie.app.model.sql.ContentDb
import am5800.harmonie.app.model.sql.SqlLemma
import am5800.harmonie.app.model.sql.query1

class SqlLemmaMeaningsProvider(private val contentDb: ContentDb) : LemmaMeaningsProvider {
  override fun getMeaningsAsSingleString(lemma: Lemma, meaningsLanguage: Language): String? {

    val sqlLemma = lemma as SqlLemma
    val query = """
      SELECT meanings FROM meanings
        WHERE lemmaId = ${sqlLemma.sqlId} AND meaningsLanguage='${meaningsLanguage.code}'
    """

    return contentDb.query1<String>(query).singleOrNull()
  }
}