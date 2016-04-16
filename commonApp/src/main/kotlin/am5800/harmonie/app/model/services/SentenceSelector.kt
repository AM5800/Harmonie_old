package am5800.harmonie.app.model.services

import am5800.common.Language

interface SentenceSelector {
  fun findBestSentenceByAttempts(learnLanguage: Language, knownLanguage: Language): SentencePair?
}