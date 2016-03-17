package dataProcessor

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.common.db.WordOccurrence
import corpus.CorpusInfo
import corpus.CorpusRepository
import corpus.parsing.CorpusParserHandler
import corpus.parsing.CorpusParsersSet
import corpus.parsing.NegraParser
import corpus.parsing.ParsePartOfSpeech
import dataProcessor.german.GermanPostProcessor
import dataProcessor.german.MorphyCsvParser
import java.io.File

fun main(args: Array<String>) {
  val repository = CorpusRepository(File("data/corpuses"))


  val data = prepareData(repository)

  val filteredData = filterData(data)
  DbWriter().write(File("androidApp/src/dataProcessor.main/assets/content.db"), filteredData)
}

fun prepareData(repository: CorpusRepository): Data {
  val infos = repository.getCorpuses().filter { it.formatId.equals("harmonie", true) }

  val postProcessors = listOf(GermanPostProcessor(MorphyCsvParser(File("data/morphy.csv"))))
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
  // TODO fix hardcode
  parsers.parse(repository.getCorpuses().filter { it.formatId.equals("NEGRA4", true) }, handler)

  return Data(data.sentenceTranslations, data.wordOccurrences, data.difficulties, handler.wordsFrequencies)
}

class WordFrequencyCounter(private val postProcessors: List<SentencePostProcessor>, data: Data) : CorpusParserHandler() {
  private val occurrences = mutableListOf<ParseWordOccurrence>()
  private var currentPostProcessor: SentencePostProcessor? = null
  val wordsFrequencies = mutableMapOf<Word, Int>()
  var language: Language? = null

  init {
    val dataFrequencies = data.wordOccurrences.groupBy { it.word }.mapValues { it.value.count() }
    wordsFrequencies.putAll(dataFrequencies)
  }

  private var currentInfo: CorpusInfo? = null

  override fun beginCorpus(info: CorpusInfo) {
    val langCode = info.metadata["language"] ?: throw Exception("Language not set for corpus: " + info.infoFile.absolutePath)
    language = LanguageParser.parse(langCode)
    currentInfo = info

    currentPostProcessor = postProcessors.firstOrNull { it.language == language }
  }

  override fun endSentence() {
    currentPostProcessor?.processInPlace(occurrences, currentInfo!!.metadata)
    for (occurrence in occurrences) {
      val word = Word(language!!, occurrence.lemma)

      if (!wordsFrequencies.contains(word)) continue
      wordsFrequencies[word] = wordsFrequencies[word]!! + 1
    }
    occurrences.clear()
  }

  override fun word(word: String, lemma: String, pos: ParsePartOfSpeech?) {
    occurrences.add(ParseWordOccurrence(lemma, -1, -1))
  }
}

fun mergeData(left: Data, right: Data): Data {
  val occurrences = left.wordOccurrences.plus(right.wordOccurrences).distinct()
  val translations = left.sentenceTranslations.plus(right.sentenceTranslations)

  return Data(translations, occurrences, emptyMap(), emptyMap())
}

fun filterData(data: Data): Data {
  val languages = extractLanguages(data)

  val translations = mutableMapOf<Sentence, Sentence>()
  val oldOccurrences = data.wordOccurrences.groupBy { it.sentence }
  val wordOccurrences = mutableListOf<WordOccurrence>()
  val difficulties = mutableMapOf<Sentence, Int>()

  for (language in languages) {
    for ((sentence, difficultyLevel) in filterByDifficulty(data, language)) {
      val translated = data.sentenceTranslations[sentence]!!
      wordOccurrences.addAll(oldOccurrences[sentence] ?: continue)

      translations.put(sentence, translated)
      translations.put(translated, sentence)
      difficulties.put(sentence, difficultyLevel)
    }
  }

  return Data(translations, wordOccurrences, difficulties, emptyMap())
}

fun extractLanguages(data: Data): List<Language> {
  return data.sentenceTranslations.keys.groupBy { it.language }.keys.toList()
}

fun filterByDifficulty(data: Data, language: Language): Map<Sentence, Int> {
  val sentences = data.sentenceTranslations.map { if (it.key.language == language) it.key else it.value }.toList()
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
      .take(10000)

  val actualCount = sentencesWithDifficulties.size
  val requiredBuckets = 100
  val bucketSize = if (actualCount < requiredBuckets) requiredBuckets else actualCount / requiredBuckets

  return sentencesWithDifficulties.mapIndexed { i, pair -> Pair(pair.first, i / bucketSize) }
      .toMap()
}

private fun acceptOptimalSentenceSize(language: Language, size: Int): Boolean {
  if (language == Language.Japanese) return size >= 3 && size < 10
  return size > 4 && size < 15
}


