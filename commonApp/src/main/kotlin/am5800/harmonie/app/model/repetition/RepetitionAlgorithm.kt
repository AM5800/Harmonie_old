package am5800.harmonie.app.model.repetition

import org.joda.time.DateTime

interface RepetitionAlgorithm {
  fun getNextDueDate(attempts: List<Attempt>): DateTime
  fun getBinaryScore(attempts: List<Attempt>): LearnScore?
}