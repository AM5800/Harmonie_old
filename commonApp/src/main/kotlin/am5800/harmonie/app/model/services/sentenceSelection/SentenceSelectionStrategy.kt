package am5800.harmonie.app.model.services.sentenceSelection

import am5800.common.Language
import am5800.harmonie.app.model.services.sentencesAndWords.SentencePair

interface SentenceSelectionStrategy {
  fun findBestSentenceByAttempts(learnLanguage: Language, knownLanguage: Language): SentencePair?
}