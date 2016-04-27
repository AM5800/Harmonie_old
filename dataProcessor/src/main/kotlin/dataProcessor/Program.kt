package dataProcessor

import am5800.common.Language
import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence
import dataProcessor.corpus.CorpusInfo
import dataProcessor.corpus.CorpusRepository
import dataProcessor.corpus.parsing.CorpusParsersSet
import dataProcessor.corpus.parsing.NegraParser
import dataProcessor.english.EnglishPostProcessor
import dataProcessor.german.GermanPostProcessor
import dataProcessor.german.MorphyCsvParser
import dataProcessor.german.normalizeGermanWord
import java.io.File

fun main(args: Array<String>) {
  val repository = CorpusRepository(File("data/corpuses"))

  run(repository, repository.getCorpuses().filter { it.formatId.equals("harmonie", true) }, File("androidApp/src/main/assets/content.db"))
  run(repository, repository.getCorpuses().filter { it.formatId.equals("harmonie-test", true) }, File("data/test.db"))
}

private fun run(repository: CorpusRepository, corpuses: List<CorpusInfo>, file: File) {
  val data = prepareData(repository, corpuses)
  val filteredData = filterData(data)
  val processedData = processFillTheGap(filteredData)
  DbWriter().write(file, processedData)
}

fun processFillTheGap(data: Data): Data {
  val seinForms = listOf("sein", "bin", "ist", "bist", "seid", "sind", "war", "gewesen")
  val articleFormsDe = listOf("ein", "eine", "einer", "eines", "einen", "einem", "das", "der", "die", "den", "dem")
  val articleFormsEn = listOf("a", "an", "the")

  val acceptedFormsDe = mutableMapOf<String, String>()
  val acceptedFormsEn = mutableMapOf<String, String>()
  acceptedFormsDe.putAll(seinForms.map { Pair(it, "de:sein") })
  acceptedFormsDe.putAll(articleFormsDe.map { Pair(it, "de:article") })
  acceptedFormsEn.putAll(articleFormsEn.map { Pair(it, "en:article") })

  val result = mutableListOf<FormOccurrence>()
  for (occurrence in data.wordOccurrences) {
    if (occurrence.sentence.language == Language.German) {
      val form = normalizeGermanWord(occurrence.getForm())
      val topic = acceptedFormsDe[form] ?: continue
      result.add(FormOccurrence(form, topic, occurrence))
    } else if (occurrence.sentence.language == Language.English) {
      val form = occurrence.getForm().toLowerCase().trim()
      val topic = acceptedFormsEn[form] ?: continue
      result.add(FormOccurrence(form, topic, occurrence))
    }
  }

  return Data(data.sentenceTranslations, data.wordOccurrences, data.difficulties, data.realWorldWordsCount, result)
}

fun prepareData(repository: CorpusRepository, infos: List<CorpusInfo>): Data {
  val postProcessors = listOf(GermanPostProcessor(MorphyCsvParser(File("data/morphy.csv"))), EnglishPostProcessor())
  val parser = HarmonieParallelSentencesParser(postProcessors)

  val initial: Data? = null
  val result = infos.fold(initial, { acc, info ->
    val data = parser.parse(info)
    if (acc == null) return@fold data
    else mergeData(acc, data)
  })!!

  return computeFrequencies(result, repository, postProcessors)
}

fun computeFrequencies(data: Data, repository: CorpusRepository, postProcessors: List<SentencePostProcessor>): Data {
  val parsers = CorpusParsersSet()
  parsers.registerParser(NegraParser())

  val handler = WordFrequencyCounter(postProcessors, data)
  val disambCounter = LemmaDisambiguationCounter()
  // TODO fix hardcode
  parsers.parse(repository.getCorpuses().filter { it.formatId.equals("NEGRA4", true) }, handler, disambCounter)

  disambCounter.print()

  return Data(data.sentenceTranslations, data.wordOccurrences, data.difficulties, handler.wordCounts, emptyList())
}

fun mergeData(left: Data, right: Data): Data {
  val occurrences = left.wordOccurrences.plus(right.wordOccurrences).distinct()
  val translations = left.sentenceTranslations.plus(right.sentenceTranslations)

  return Data(translations, occurrences, emptyMap(), emptyMap(), emptyList())
}

fun filterData(data: Data): Data {
  val languages = extractLanguages(data)

  val translations = mutableMapOf<Sentence, Sentence>()
  val oldOccurrences = data.wordOccurrences.groupBy { it.sentence }
  val wordOccurrences = mutableListOf<WordOccurrence>()
  val difficulties = mutableMapOf<Sentence, Int>()
  val wordCounts = mutableMapOf<Word, Int>()

  for (language in languages) {
    for ((sentence, difficultyLevel) in filterByDifficulty(data, language)) {
      val translated = data.sentenceTranslations[sentence]!!
      val wordsInSentence = oldOccurrences[sentence] ?: continue
      wordOccurrences.addAll(wordsInSentence)

      for (occurrence in wordsInSentence) {
        val prevValue = data.realWorldWordsCount[occurrence.word] ?: continue
        wordCounts[occurrence.word] = prevValue
      }

      translations.put(sentence, translated)
      translations.put(translated, sentence)
      difficulties.put(sentence, difficultyLevel)
    }
  }

  return Data(translations, wordOccurrences, difficulties, wordCounts, emptyList())
}

fun extractLanguages(data: Data): List<Language> {
  return data.sentenceTranslations.keys.groupBy { it.language }.keys.toList()
}

fun filterByDifficulty(data: Data, language: Language): Map<Sentence, Int> {
  val sentences = data.sentenceTranslations.filter { it.key.language == language }.map { it.key }.toList()
  val occurrences = data.wordOccurrences.filter { it.word.language == language }
  val sentenceToOccurrences: Map<Sentence, List<WordOccurrence>> = occurrences.groupBy { it.sentence }
  val totalWordsCount = data.realWorldWordsCount.values.sum()
  val wordFrequencies = data.realWorldWordsCount.mapValues { it.value / totalWordsCount.toDouble() }

  val sentencesWithDifficulties = sentences
      .map {
        Pair(it, sentenceToOccurrences[it] ?: emptyList())
      }
      .toMap()
      .mapValues { it.value.map { occ -> wordFrequencies[occ.word] ?: 0.0 } }
      .filter { acceptOptimalSentenceSize(language, it.value.size) }
      .mapValues { it.value.fold(1.0, { i, d -> i * d }) }
      .toList()
      .sortedByDescending { it.second }
      .take(15000)

  val actualCount = sentencesWithDifficulties.size
  val requiredBuckets = 10
  val bucketSize = if (actualCount < requiredBuckets) requiredBuckets else actualCount / requiredBuckets

  return sentencesWithDifficulties.mapIndexed { i, pair -> Pair(pair.first, i / bucketSize) }
      .toMap()
}

private fun acceptOptimalSentenceSize(language: Language, size: Int): Boolean {
  if (language == Language.Japanese) return true
  return size > 4 && size < 15
}


