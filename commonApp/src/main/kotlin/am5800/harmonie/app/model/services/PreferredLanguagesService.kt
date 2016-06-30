package am5800.harmonie.app.model.services

import am5800.common.Language
import am5800.common.WithCounter
import am5800.common.utils.properties.Property

interface PreferredLanguagesService {
  fun getAvailableKnownLanguages(): Collection<Language>
  fun getAvailableLearnLanguages(language: Language): Collection<WithCounter<Language>>
  val configurationRequired: Boolean
  val selectedKnownLanguages: Property<List<Language>>
  val selectedLearnLanguages: Property<List<Language>>
}