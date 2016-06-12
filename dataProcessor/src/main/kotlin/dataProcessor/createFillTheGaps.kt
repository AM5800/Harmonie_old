package dataProcessor

import am5800.common.Language
import dataProcessor.db.FillTheGapsWriter

fun createFillTheGaps(parseResult: ParseResult, fillTheGapsWriter: FillTheGapsWriter) {
  val seinForms = setOf("sein", "bin", "ist", "bist", "seid", "sind", "war", "gewesen")
  val articleFormsDe = setOf("ein", "eine", "einer", "eines", "einen", "einem", "das", "der", "die", "den", "dem")
  val articleFormsEn = setOf("a", "an", "the")

  val result = mutableListOf<FormOccurrence>()
  for (occurrence in parseResult.occurrences) {
    val form = occurrence.getForm().toLowerCase().trim()
    val pos = parseResult.occurrencePos[occurrence]

    if (occurrence.sentence.language == Language.German) {
      if (pos == PartOfSpeech.Verb && seinForms.contains(form)) {
        result.add(FormOccurrence(form, "de:sein", occurrence))
      } else if (pos == PartOfSpeech.Article && articleFormsDe.contains(form)) {
        result.add(FormOccurrence(form, "de:article", occurrence))
      }

    } else if (occurrence.sentence.language == Language.English) {
      if (pos == PartOfSpeech.Article && articleFormsEn.contains(form)) {
        result.add(FormOccurrence(form, "en:article", occurrence))
      }
    }
  }

  fillTheGapsWriter.write(result)
}


