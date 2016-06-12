package dataProcessor

import am5800.common.Language
import am5800.common.Word
import java.io.File

fun main(args: Array<String>) {
  val corpusDir = File("data/corpus")
  val corpuses = corpusDir
      .listFiles { file -> file.extension.equals("xml", true) }.toList()

  val counts = loadCounts(corpusDir)

  run(corpuses, File("androidApp/src/main/assets/content.db"), counts)
  run(listOf(File(corpusDir, "test")), File("data/test.db"), counts)
}

fun loadCounts(corpusDir: File): Map<Word, Int> {
  return CountsParser().parse(File(corpusDir, "counts"))
}

private fun run(corpuses: Collection<File>, outFile: File, counts: Map<Word, Int>) {
  val data = loadData(corpuses)
  val processedData = createFillTheGaps(data)
  DbWriter().write(outFile, setCounts(processedData, counts))
}

fun setCounts(data: Data, counts: Map<Word, Int>): Data {
  val result = mutableMapOf<Word, Int>()
  for (word in data.wordOccurrences.map { it.word }) {
    val count = counts[word] ?: continue
    result.put(word, count)
  }

  return Data(data.sentences, data.sentenceTranslations, data.wordOccurrences, result, data.fillTheGapOccurrences, data.occurrencePos)
}

fun createFillTheGaps(data: Data): Data {
  val seinForms = setOf("sein", "bin", "ist", "bist", "seid", "sind", "war", "gewesen")
  val articleFormsDe = setOf("ein", "eine", "einer", "eines", "einen", "einem", "das", "der", "die", "den", "dem")
  val articleFormsEn = setOf("a", "an", "the")

  val result = mutableListOf<FormOccurrence>()
  for (occurrence in data.wordOccurrences) {
    val form = occurrence.getForm().toLowerCase().trim()
    val pos = data.occurrencePos[occurrence]

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

  return Data(data.sentences, data.sentenceTranslations, data.wordOccurrences, data.realWorldWordsCount, result, data.occurrencePos)
}

fun loadData(corpuses: Collection<File>): Data {
  val parser = HarmonieParallelSentencesParser()

  val initial: Data? = null
  val result = corpuses.fold(initial, { acc, path ->
    val data = parser.parse(path)
    if (acc == null) return@fold data
    else mergeData(acc, data)
  })!!

  return result
}

fun mergeData(left: Data, right: Data): Data {
  val sentences = left.sentences.plus(right.sentences).distinct()
  val occurrences = left.wordOccurrences.plus(right.wordOccurrences).distinct()
  val translations = left.sentenceTranslations.plus(right.sentenceTranslations)
  val occurrencePos = left.occurrencePos.toList().plus(right.occurrencePos.toList()).toMap()

  return Data(sentences, translations, occurrences, emptyMap(), emptyList(), occurrencePos)
}


