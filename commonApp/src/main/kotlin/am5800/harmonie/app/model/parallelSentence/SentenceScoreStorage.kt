package am5800.harmonie.app.model.parallelSentence

import am5800.common.Sentence


interface SentenceScoreStorage {
  fun getScores(sentences: List<Sentence>): List<Pair<Sentence, SentenceScore?>>
  fun setScore(sentence: Sentence, score: SentenceScore)
}