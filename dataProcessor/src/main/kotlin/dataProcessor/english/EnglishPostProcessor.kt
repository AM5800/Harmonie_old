package dataProcessor.english

import am5800.common.Language
import dataProcessor.ParseWordOccurrence
import dataProcessor.PostProcessorBase


class EnglishPostProcessor : PostProcessorBase() {
  override val language: Language = Language.English

  override fun processInPlace(occurrences: MutableList<ParseWordOccurrence>, metadata: Map<String, String>) {
    if (!isLemmatized(metadata)) throw Exception("English lemmatization is not yet supported")

    for (occurrence in occurrences.toList()) {
      if (shouldDelete(occurrence)) occurrences.remove(occurrence)
      occurrence.lemma = normalize(occurrence.lemma)
    }
  }

  private fun normalize(str: String): String {
    return str.toLowerCase().trim()
  }
}