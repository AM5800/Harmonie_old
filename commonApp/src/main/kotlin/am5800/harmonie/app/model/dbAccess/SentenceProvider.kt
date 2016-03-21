package am5800.harmonie.app.model.dbAccess

import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.common.db.WordOccurrence

interface SentenceProvider {
  fun getWordsInSentence(sentence: Sentence): List<Word>
  fun getOccurrences(sentence: Sentence): List<WordOccurrence>
}