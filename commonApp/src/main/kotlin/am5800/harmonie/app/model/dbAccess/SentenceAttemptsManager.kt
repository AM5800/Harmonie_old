package am5800.harmonie.app.model.dbAccess

import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.harmonie.app.model.flow.ParallelSentenceUserScore


interface SentenceAttemptsManager {
  fun submitAttempt(tag: ParallelSentenceUserScore, sentence: Sentence)
  fun getWordFrequency(word: Word, tag: ParallelSentenceUserScore): Double
  fun getTagFrequency(tag: ParallelSentenceUserScore): Double
}