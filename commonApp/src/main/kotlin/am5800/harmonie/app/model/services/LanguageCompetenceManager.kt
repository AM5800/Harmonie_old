package am5800.harmonie.app.model.services

import am5800.common.Language
import am5800.harmonie.app.model.services.flow.LanguageCompetence

interface LanguageCompetenceManager {
  fun isKnown(language: Language): Boolean

  val languageCompetence: List<LanguageCompetence>
}