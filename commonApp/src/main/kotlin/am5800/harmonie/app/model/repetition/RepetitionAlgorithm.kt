package am5800.harmonie.app.model.repetition

import org.joda.time.DateTime

enum class BinaryLearnScore {
  Good, Bad
}

interface RepetitionAlgorithm {
  fun getNextDueDate(attempts: List<Attempt>): DateTime
  fun getScoreAsInt(attempts: List<Attempt>): Int
  fun getBinaryScore(attempts: List<Attempt>): BinaryLearnScore?
}