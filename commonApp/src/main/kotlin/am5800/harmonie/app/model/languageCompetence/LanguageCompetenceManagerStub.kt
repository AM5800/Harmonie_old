package am5800.harmonie.app.model.languageCompetence

import am5800.common.Language

class LanguageCompetenceManagerStub : LanguageCompetenceManager {
  override fun isKnown(language: Language): Boolean {
    return languageCompetence.any { it.language == language }
  }

  override val languageCompetence: List<LanguageCompetence>
      = listOf(LanguageCompetence(Language.English, Competence.Fluent), LanguageCompetence(Language.Russian, Competence.Native))
}