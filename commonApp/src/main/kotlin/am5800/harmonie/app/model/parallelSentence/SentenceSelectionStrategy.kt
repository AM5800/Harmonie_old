package am5800.harmonie.app.model.parallelSentence

import am5800.common.Sentence


interface SentenceSelectionStrategy {
  fun select(sentences: List<Pair<Sentence, SentenceScore?>>): Sentence?
}