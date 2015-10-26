package am5800.harmonie.model

import org.joda.time.DateTime

public enum class WordLearnLevel {
    NotStarted,
    JustStarted,
    BarelyKnown,
    Confident,
    Known,
}

public interface RepetitionAlgorithm {
    fun getNextDueDate(attempts: List<Attempt>): DateTime
    fun computeLevel(attempts : List<Attempt>) : WordLearnLevel
}