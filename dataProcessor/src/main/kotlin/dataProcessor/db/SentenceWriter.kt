package dataProcessor.db

import am5800.common.LemmaOccurrence
import am5800.common.Sentence


interface SentenceWriter {
  fun write(sentencePairs: List<Pair<Sentence, Sentence>>, occurrences: List<LemmaOccurrence>)
}