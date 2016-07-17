package am5800.harmonie.app.model.services

import am5800.common.Language
import am5800.harmonie.app.model.features.flow.Competence
import am5800.harmonie.app.model.features.flow.LanguageCompetence

class LanguageCompetenceManagerStub : LanguageCompetenceManager {
  override fun isKnown(language: Language): Boolean {
    return languageCompetence.any { it.language == language }
  }

  override val languageCompetence: List<LanguageCompetence>
      = listOf(LanguageCompetence(Language.English, Competence.Fluent), LanguageCompetence(Language.Russian, Competence.Native))
}