package am5800.harmonie.app.model.services.impl

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.db.ContentDbConstants
import am5800.common.utils.Lifetime
import am5800.common.utils.convert
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.services.ContentDb
import am5800.harmonie.app.model.services.KeyValueDatabase
import am5800.harmonie.app.model.services.PreferredLanguagesService
import am5800.harmonie.app.model.services.query2

class SqlPreferredLanguagesService(keyValueDatabase: KeyValueDatabase,
                                   lifetime: Lifetime,
                                   contentDb: ContentDb,
                                   private val debugOptions: DebugOptions) : PreferredLanguagesService {
  override val knownLanguages = keyValueDatabase.createProperty(lifetime, "knownLanguages", "").convert({ stringToLanguages(it) }, { languagesToString(it) })
  override val learnLanguages = keyValueDatabase.createProperty(lifetime, "learnLanguages", "").convert({ stringToLanguages(it) }, { languagesToString(it) })

  private val supportedDirections = mutableListOf<Pair<Language, Language>>()

  init {
    if (debugOptions.dropPreferredLanguagesOnStart) {
      knownLanguages.value = emptyList()
      learnLanguages.value = emptyList()
    }

    val directions = ContentDbConstants.supportedLearningDirections
    val queryResult = contentDb.query2<String, String>("SELECT languageFrom, languageTo FROM $directions")
    supportedDirections.addAll(queryResult.map { Pair(LanguageParser.parse(it.first), LanguageParser.parse(it.second)) })
  }

  override fun getAvailableLanguages(): Collection<Language> {
    return supportedDirections.map { it.first }
  }

  override fun getAvailableTranslations(language: Language): Collection<Language> {
    return supportedDirections.filter { it.first == language }.map { it.second }
  }

  override val configurationRequired: Boolean
    get() = knownLanguages.value!!.isEmpty() || learnLanguages.value!!.isEmpty()

  private fun languagesToString(languages: List<Language>?): String {
    return languages!!.map { it.code }.joinToString (", ")
  }

  private fun stringToLanguages(string: String?): List<Language> {
    return string!!.split(",").map { LanguageParser.tryParse(it.trim()) }.filterNotNull()
  }
}