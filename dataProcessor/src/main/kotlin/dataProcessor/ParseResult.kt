package dataProcessor

import am5800.common.Sentence
import am5800.common.WordOccurrence

interface ParseResult {
  val occurrencePos: Map<WordOccurrence, PartOfSpeech>
  val occurrences: Set<WordOccurrence>
  val translations: Map<Sentence, Sentence>
  val sentences: List<Sentence>
}

fun Collection<ParseResult>.merge(): ParseResult {
  val sentences = this.flatMap { it.sentences }.distinct()
  val occurrences = this.flatMap { it.occurrences }.distinct().toSet()
  val translations = this.flatMap { it.translations.toList() }.distinct().toMap()
  val occurrencesPos = this.flatMap { it.occurrencePos.toList() }.distinct().toMap()

  return ParseResultImpl(occurrencesPos, occurrences, translations, sentences)
}