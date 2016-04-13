package am5800.harmonie.app.model.services

import am5800.common.*

interface SentenceProvider {
  fun getWordsInSentence(sentence: Sentence): List<Word>
  fun getOccurrences(sentence: Sentence): List<WordOccurrence>

  fun getAvailableLanguagePairs(): Collection<WithCounter<LanguagePair>>
}