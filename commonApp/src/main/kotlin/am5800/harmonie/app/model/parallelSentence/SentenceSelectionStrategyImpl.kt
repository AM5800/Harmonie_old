package am5800.harmonie.app.model.parallelSentence

import am5800.common.Sentence

class SentenceSelectionStrategyImpl : SentenceSelectionStrategy {
  override fun select(sentences: List<Pair<Sentence, SentenceScore?>>): Sentence? {
    if (sentences.isEmpty()) return null

    val firstUnknown = sentences.firstOrNull { it.second == null }
    if (firstUnknown != null) return firstUnknown.first

    val firstNonBlackout = sentences.firstOrNull { it.second != SentenceScore.TotalBlackout } ?: return sentences.first().first

    return firstNonBlackout.first
  }
}