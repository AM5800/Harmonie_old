package am5800.harmonie.app.model.services

import am5800.common.Language
import am5800.common.Sentence
import am5800.common.Word

class SentenceSelectorResult(val question: Sentence, val answer: Sentence, val highlightedWords: Set<Word>) {
  constructor(sentencePair: SentencePair) : this(sentencePair.learnLanguageSentence, sentencePair.knownLanguageSentence, emptySet())
}

interface SentenceSelector {
  fun findBestSentenceByAttempts(learnLanguage: Language, knownLanguage: Language): SentenceSelectorResult?
}