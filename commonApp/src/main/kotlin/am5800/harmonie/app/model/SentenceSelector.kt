package am5800.harmonie.app.model

import am5800.common.Language
import am5800.common.db.Sentence
import am5800.common.db.Word

class SentenceSelectorResult(val question: Sentence, val answer: Sentence, val highlightedWords: Set<Word>)

interface SentenceSelector {
  fun findBestSentence(languageFrom: Language, languageTo: Language): SentenceSelectorResult?
}