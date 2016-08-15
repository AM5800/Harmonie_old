package am5800.harmonie.app.model.lemmasMeaning

import am5800.common.Language
import am5800.common.Lemma
import am5800.harmonie.app.model.sql.ContentDb
import am5800.harmonie.app.model.sql.SqlLemma
import am5800.harmonie.app.model.sql.query1

class SqlLemmaTranslationsProvider(private val contentDb: ContentDb) : LemmaTranslationsProvider {
  override fun getTranslations(lemma: Lemma, translationsLanguage: Language): List<String> {

    val sqlLemma = lemma as SqlLemma
    val query = """
      SELECT translations FROM translations
        WHERE lemmaId = ${sqlLemma.sqlId} AND language ='${translationsLanguage.code}'
    """

    val str = contentDb.query1<String>(query).singleOrNull() ?: return emptyList()
    return str.split(";>")
  }
}