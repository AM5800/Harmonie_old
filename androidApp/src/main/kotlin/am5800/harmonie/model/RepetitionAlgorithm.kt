package am5800.harmonie.model

import org.joda.time.DateTime

enum class WordLearnLevel {
  NotStarted,
  JustStarted,
  BarelyKnown,
  Confident,
  Known,
}

interface RepetitionAlgorithm {
  fun getNextDueDate(attempts: List<Attempt>): DateTime
  fun computeLevel(attempts: List<Attempt>): WordLearnLevel
}