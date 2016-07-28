package am5800.harmonie.app.model.languageCompetence

import am5800.common.Language

interface LanguageCompetenceManager {
  fun isKnown(language: Language): Boolean

  val languageCompetence: List<LanguageCompetence>
}