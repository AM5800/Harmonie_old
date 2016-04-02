package am5800.harmonie.app.model.dbAccess.sql

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.utils.Lifetime
import am5800.common.utils.convert
import am5800.harmonie.app.model.dbAccess.KeyValueDatabase
import am5800.harmonie.app.model.dbAccess.PreferredLanguagesService

class SqlPreferredLanguagesService(keyValueDatabase: KeyValueDatabase, lifetime: Lifetime) : PreferredLanguagesService, ContentDbConsumer {
  override val knownLanguages = keyValueDatabase.createProperty(lifetime, "knownLanguages", "").convert({ stringToLanguages(it) }, { languagesToString(it) })
  override val learnLanguages = keyValueDatabase.createProperty(lifetime, "learnLanguages", "").convert({ stringToLanguages(it) }, { languagesToString(it) })

  private var contentDb: ContentDb? = null

  override fun dbInitialized(db: ContentDb) {
    contentDb = db
  }

  override fun getAvailableLanguages(): Collection<Language> {
    val query = "SELECT DISTINCT(language) FROM sentences"
    return contentDb!!.query1<String>(query).map { LanguageParser.parse(it) }
  }

  override fun getAvailableTranslations(language: Language): Collection<Language> {
    val query = """
    SELECT DISTINCT(s2.language) FROM sentenceMapping
      INNER JOIN sentences AS s1
        ON s1.id = sentenceMapping.key
      INNER JOIN sentences AS s2
        ON s2.id = sentenceMapping.value
      WHERE s1.language = '${language.code}'
    """
    return contentDb!!.query1<String>(query).map { LanguageParser.parse(it) }
  }

  override val configurationRequired: Boolean
    get() = knownLanguages.value!!.isEmpty() || learnLanguages.value!!.isEmpty()

  private fun languagesToString(languages: List<Language>?): String {
    return languages!!.map { it.code }.joinToString (", ")
  }

  private fun stringToLanguages(string: String?): List<Language> {
    return string!!.split(",").map { LanguageParser.tryParse(it) }.filterNotNull()
  }
}