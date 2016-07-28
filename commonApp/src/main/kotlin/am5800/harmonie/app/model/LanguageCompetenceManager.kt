package am5800.harmonie.app.model

import am5800.common.Language
import am5800.harmonie.app.model.flow.LanguageCompetence

interface LanguageCompetenceManager {
  fun isKnown(language: Language): Boolean

  val languageCompetence: List<LanguageCompetence>
}