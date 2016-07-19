package am5800.harmonie.app.model.services.sentenceSelection

import am5800.common.Language
import am5800.harmonie.app.model.services.flow.LanguageCompetence
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndTranslation

interface SentenceSelectionStrategy {
  fun findBestSentenceByAttempts(learnLanguage: Language, competence: List<LanguageCompetence>): SentenceAndTranslation?
}