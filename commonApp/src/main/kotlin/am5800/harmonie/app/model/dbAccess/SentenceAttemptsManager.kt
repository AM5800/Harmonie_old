package am5800.harmonie.app.model.dbAccess

import am5800.common.db.DbWord
import am5800.harmonie.app.model.flow.ParallelSentenceUserScore


interface SentenceAttemptsManager {
  fun submitAttempt(tag: ParallelSentenceUserScore, wordsInSentence: List<DbWord>)
  fun getWordFrequency(word: DbWord, tag: ParallelSentenceUserScore): Double
  fun getTagFrequency(tag: ParallelSentenceUserScore): Double
}