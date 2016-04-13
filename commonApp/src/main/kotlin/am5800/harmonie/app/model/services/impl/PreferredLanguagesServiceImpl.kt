package am5800.harmonie.app.model.services.impl

import am5800.common.Language
import am5800.common.LanguagePair
import am5800.common.LanguageParser
import am5800.common.utils.Lifetime
import am5800.common.utils.convert
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.flow.FlowItemProvider
import am5800.harmonie.app.model.features.flow.LanguageCategory
import am5800.harmonie.app.model.services.KeyValueDatabase
import am5800.harmonie.app.model.services.PreferredLanguagesService

class PreferredLanguagesServiceImpl(keyValueDatabase: KeyValueDatabase,
                                    lifetime: Lifetime,
                                    itemProviders: List<FlowItemProvider>,
                                    private val debugOptions: DebugOptions) : PreferredLanguagesService {
  override val knownLanguages = keyValueDatabase.createProperty(lifetime, "knownLanguages", "").convert({ stringToLanguages(it) }, { languagesToString(it) })
  override val learnLanguages = keyValueDatabase.createProperty(lifetime, "learnLanguages", "").convert({ stringToLanguages(it) }, { languagesToString(it) })

  private val supportedDirections = mutableListOf<LanguagePair>()

  init {
    if (debugOptions.dropPreferredLanguagesOnStart) {
      knownLanguages.value = emptyList()
      learnLanguages.value = emptyList()
    }

    val directions = itemProviders
        .flatMap { it.supportedCategories }
        .filterIsInstance<LanguageCategory>()
        .map { LanguagePair(it.knownLanguage, it.learnLanguage) }
        .distinct()

    supportedDirections.addAll(directions)
  }

  override fun getAvailableKnownLanguages(): Collection<Language> {
    return supportedDirections.map { it.knownLanguage }.distinct()
  }

  override fun getAvailableLearnLanguages(language: Language): Collection<Language> {
    return supportedDirections.filter { it.knownLanguage == language }.map { it.learnLanguage }
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