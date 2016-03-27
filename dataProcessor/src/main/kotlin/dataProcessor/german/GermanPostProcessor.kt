package dataProcessor.german

import am5800.common.Language
import dataProcessor.ParseWordOccurrence
import dataProcessor.SentencePostProcessor

class GermanPostProcessor(private val lemmatizer: GermanLemmatizer) : SentencePostProcessor {
  override val language: Language = Language.German

  override fun processInPlace(occurrences: MutableList<ParseWordOccurrence>, metadata: Map<String, String>) {
    val linearSequence = mutableListOf<ParseWordOccurrence>()
    for (occurrence in occurrences.toList()) {
      if (shouldDelete(occurrence)) occurrences.remove(occurrence)
      else {
        if (!isTokenized(metadata)) {
          val lemma = lemmatizer.tryFindLemma(occurrence.lemma)
          if (lemma != null) occurrence.lemma = lemma
        } else occurrence.lemma = lemmatizer.normalize(occurrence.lemma)
        linearSequence.add(occurrence)
      }

      if (occurrences.isEmpty()) return
      if (needToProcessPrefixes(metadata) && (endsSequence(occurrence) || occurrence == occurrences.last())) {
        processSequence(linearSequence)
        linearSequence.clear()
      }
    }
  }

  private fun needToProcessPrefixes(metadata: Map<String, String>): Boolean {
    val value = metadata["dePrefixes"] ?: return true
    return !value.toBoolean()
  }

  private fun isTokenized(metadata: Map<String, String>): Boolean {
    val value = metadata["lemmatized"] ?: return false
    return value.toBoolean()
  }

  private fun processSequence(linearSequence: List<ParseWordOccurrence>) {
    if (linearSequence.size < 2) return

    val possiblePrefix = linearSequence.last()
    if (lemmatizer.looksLikeSeparablePrefix(possiblePrefix.lemma)) {
      for (occurrence in linearSequence.dropLast(1).reversed()) {
        val verb = lemmatizer.tryFindVerb(occurrence.lemma, possiblePrefix.lemma) ?: continue

        possiblePrefix.lemma = verb
        occurrence.lemma = verb
        return
      }
    }
  }

  private fun endsSequence(occurrence: ParseWordOccurrence): Boolean {
    return isPunctuation(occurrence.lemma)
  }

  private fun shouldDelete(occurrence: ParseWordOccurrence): Boolean {
    if (occurrence.lemma.any { c -> c.isDigit() }) return true
    if (isPunctuation(occurrence.lemma)) return true

    return false
  }

  private fun isPunctuation(s: String): Boolean {
    val chars = "[,.'\"'`!?<>{}():]-".toCharArray().toSet()
    return s.any() && s.all { c -> chars.contains(c) }
  }
}