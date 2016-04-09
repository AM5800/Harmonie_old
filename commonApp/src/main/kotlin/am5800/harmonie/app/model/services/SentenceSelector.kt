package am5800.harmonie.app.model.services

import am5800.common.Language
import am5800.common.Sentence
import am5800.common.Word

class SentenceSelectorResult(val question: Sentence, val answer: Sentence, val highlightedWords: Set<Word>)

interface SentenceSelector {
  fun findBestSentenceByAttempts(languageFrom: Language, languagesTo: Collection<Language>): SentenceSelectorResult?
}