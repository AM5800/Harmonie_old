package am5800.harmonie.app.model.flow

import am5800.common.Language

enum class Competence(val value: Int) {
  Native(4),
  Fluent(3),
  Advanced(2),
  Intermediate(1),
  Beginner(0),
}

class LanguageCompetence(val language: Language, val competence: Competence)