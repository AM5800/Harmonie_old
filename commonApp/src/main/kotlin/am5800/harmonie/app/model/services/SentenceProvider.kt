package am5800.harmonie.app.model.services

import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence
import am5800.harmonie.app.model.LanguagePair

interface SentenceProvider {
  fun getWordsInSentence(sentence: Sentence): List<Word>
  fun getOccurrences(sentence: Sentence): List<WordOccurrence>

  fun getAvailableLanguagePairs(): Collection<LanguagePair>
}