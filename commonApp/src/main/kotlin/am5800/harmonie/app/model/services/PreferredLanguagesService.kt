package am5800.harmonie.app.model.services

import am5800.common.Language
import am5800.common.utils.Property

interface PreferredLanguagesService {
  fun getAvailableKnownLanguages(): Collection<Language>
  fun getAvailableLearnLanguages(language: Language): Collection<Language>
  val configurationRequired: Boolean
  val knownLanguages: Property<List<Language>>
  val learnLanguages: Property<List<Language>>
}