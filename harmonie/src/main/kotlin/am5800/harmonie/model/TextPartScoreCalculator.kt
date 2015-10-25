package am5800.harmonie.model

public class TextPartScoreCalculator(private val repetitionAlg: RepetitionAlgorithm,
                                     private val historyManager : AttemptsHistoryManager) {
    private val scoreMap = mapOf(
            Pair(WordLearnLevel.NotStarted, 0.0),
            Pair(WordLearnLevel.JustStarted, 0.25),
            Pair(WordLearnLevel.BarelyKnown, 0.5),
            Pair(WordLearnLevel.Confident, 0.75),
            Pair(WordLearnLevel.Known, 1.0)
    )

    public fun calculate(part: TextPart): Double {
        val sum = part.entities.sumByDouble {
            val attempts = historyManager.getAttempts(it)
            scoreMap[repetitionAlg.computeLevel (attempts)]!!
        }
        return sum / part.entities.count()
    }
}