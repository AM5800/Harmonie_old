package am5800.harmonie.app.model.services

import am5800.common.*
import am5800.harmonie.app.model.services.impl.SqlWord

data class SentencePair(val knownLanguageSentence: Sentence, val learnLanguageSentence: Sentence)

interface SentenceProvider {
  fun getWordsInSentence(sentence: Sentence): List<Word>
  fun getOccurrences(sentence: Sentence): List<WordOccurrence>

  fun getAvailableLanguagePairs(): Collection<WithCounter<LanguagePair>>
  fun getRandomSentencePair(learnLanguage: Language, knownLanguage: Language): SentencePair?
  fun findEasiestMatchingSentence(learnLanguage: Language, knownLanguage: Language, containingWords: List<SqlWord>): SentencePair?
}