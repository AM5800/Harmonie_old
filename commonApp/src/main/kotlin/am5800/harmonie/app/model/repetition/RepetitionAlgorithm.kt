package am5800.harmonie.app.model.repetition

import org.joda.time.DateTime

enum class LearnScore {
  Good, Bad, Unknown
}

interface RepetitionAlgorithm {
  fun getNextDueDate(attempts: List<Attempt>): DateTime
  fun getScoreAsInt(attempts: List<Attempt>): Int
  fun getScoreAsEnum(attempts: List<Attempt>): LearnScore
}