package am5800.harmonie.model

import am5800.harmonie.model.Attempt
import org.joda.time
import org.joda.time.DateTime
import java.util.*

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