package am5800.harmonie.app.model.dbAccess

import am5800.common.db.DbSentence
import am5800.common.db.DbWord
import am5800.harmonie.app.model.flow.ParallelSentenceUserScore


interface SentenceAttemptsManager {
  fun submitAttempt(tag: ParallelSentenceUserScore, sentence: DbSentence)
  fun getWordFrequency(word: DbWord, tag: ParallelSentenceUserScore): Double
  fun getTagFrequency(tag: ParallelSentenceUserScore): Double
}