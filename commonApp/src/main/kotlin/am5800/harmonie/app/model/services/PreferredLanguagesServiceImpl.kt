package am5800.harmonie.app.model.services

import am5800.common.*
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.convert
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.flow.FlowItemProvider
import am5800.harmonie.app.model.features.flow.LanguageCategory

class PreferredLanguagesServiceImpl(keyValueDatabase: KeyValueDatabase,
                                    lifetime: Lifetime,
                                    itemProviders: List<FlowItemProvider>,
                                    debugOptions: DebugOptions) : PreferredLanguagesService {
  override val selectedKnownLanguages = keyValueDatabase.createProperty(lifetime, "knownLanguages", "").convert({ stringToLanguages(it) }, { languagesToString(it) })
  override val selectedLearnLanguages = keyValueDatabase.createProperty(lifetime, "learnLanguages", "").convert({ stringToLanguages(it) }, { languagesToString(it) })

  private val supportedDirections = mutableListOf<WithCounter<LanguagePair>>()

  init {
    if (debugOptions.dropPreferredLanguagesOnStart) {
      selectedKnownLanguages.value = emptyList()
      selectedLearnLanguages.value = emptyList()
    }

    val directions = itemProviders
        .flatMap { provider ->
          provider.supportedCategories.map { category ->
            if (category !is LanguageCategory) return@map null
            return@map WithCounter(category, provider.getAvailableDataSetSize(category))
          }.filterNotNull()
        }
        .map { WithCounter(LanguagePair(it.entity.knownLanguage, it.entity.learnLanguage), it.count) }
        .merge()

    supportedDirections.addAll(directions)
  }

  override fun getAvailableKnownLanguages(): Collection<Language> {
    return supportedDirections.map { it.entity.knownLanguage }.distinct()
  }

  override fun getAvailableLearnLanguages(language: Language): Collection<WithCounter<Language>> {
    return supportedDirections
        .filter { it.entity.knownLanguage == language }
        .map { WithCounter(it.entity.learnLanguage, it.count) }
        .merge()
  }

  override val configurationRequired: Boolean
    get() = selectedKnownLanguages.value.isEmpty() || selectedLearnLanguages.value.isEmpty()

  private fun languagesToString(languages: List<Language>?): String {
    return languages!!.map { it.code }.joinToString (", ")
  }

  private fun stringToLanguages(string: String?): List<Language> {
    return string!!.split(",").map { LanguageParser.tryParse(it.trim()) }.filterNotNull()
  }
}