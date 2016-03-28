package am5800.harmonie.app.model.dbAccess

import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence

interface SentenceProvider {
  fun getWordsInSentence(sentence: Sentence): List<Word>
  fun getOccurrences(sentence: Sentence): List<WordOccurrence>
}