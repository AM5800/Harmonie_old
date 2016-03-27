package dataProcessor

import am5800.common.Language

interface SentencePostProcessor {
  val language: Language
  fun processInPlace(occurrences: MutableList<ParseWordOccurrence>, metadata: Map<String, String>)
}