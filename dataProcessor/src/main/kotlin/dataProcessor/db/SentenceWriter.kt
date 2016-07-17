package dataProcessor.db

import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence


interface SentenceWriter {
  fun write(sentences: List<Sentence>, levels: Map<Sentence, Int?>)
  fun write(occurrences: Set<WordOccurrence>, levels: Map<Word, Int?>)
  fun write(translations: Map<Sentence, Sentence>, levels: Map<Sentence, Int?>)
}