package dataProcessor.db

import am5800.common.Sentence
import am5800.common.WordOccurrence


interface SentenceWriter {
  fun write(sentences: List<Sentence>)
  fun write(occurrences: Set<WordOccurrence>)
  fun write(translations: Map<Sentence, Sentence>)
}