import am5800.common.Language
import am5800.common.Sentence
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import corpus.CorpusInfo
import corpus.CorpusRepository
import corpus.parsing.CorpusParsersSet
import corpus.parsing.NegraParser
import ml.sentenceBreaking.splitWords
import java.io.File

fun computeParallelSentences(infos: List<CorpusInfo>): Pair<Map<Long, Long>, List<Sentence>> {
  val sentences = mutableListOf<Sentence>()
  val translations = mutableMapOf<Long, Long>()

  val sxmlCorpusParser = SxmlCorpusParser()
  for (corpus in infos) {
    val enPath = File(corpus.infoFile.parentFile, corpus.metadata["en"])
    val dePath = File(corpus.infoFile.parentFile, corpus.metadata["de"])
    val enData = sxmlCorpusParser.parse(enPath)
    val deData = sxmlCorpusParser.parse(dePath)

    for (en in enData) {
      val de = deData[en.key]!!

      sentences.add(Sentence(Language.English, en.value))
      sentences.add(Sentence(Language.German, de))
      val gi = (sentences.size - 1).toLong()
      val ei = (sentences.size - 2).toLong()

      translations.put(gi, ei)
      translations.put(ei, gi)
    }
  }

  return Pair(translations, sentences)
}

class Data(val sentences: List<Sentence>,
           val sentenceTranslations: Map<Long, Long>,
           val wordOccurrences: Multimap<Word, Long>,
           val wordFrequencies: Map<Word, Double>)

fun main(args: Array<String>) {
  val repository = CorpusRepository(File("data\\corpuses"))
  val parsersSet = CorpusParsersSet()
  parsersSet.registerParser(NegraParser())

  val data = computeData(repository, parsersSet)
  val filteredData = filterData(data)

  DbWriter().write(File("data\\db\\content.db"), filteredData)
}

fun computeData(repository: CorpusRepository, parsersSet: CorpusParsersSet): Data {
  val sentences = computeParallelSentences(repository.getCorpuses().filter { it.formatId.equals("parallel", true) })

  val occurrences = computeWordOccurrences(sentences.second)

  val frequencyHandler = WordsFrequencyHandler(occurrences.keySet())
  parsersSet.parse(repository.getCorpuses().filter { !it.formatId.equals("parallel", true) }, frequencyHandler)

  val frequencies = computeFrequencies(frequencyHandler.result, occurrences)

  return Data(sentences.second, sentences.first, occurrences, frequencies)
}

fun filterGermanSentences(data: Data): List<Int> {
  val sentenceToWords = LinkedHashMultimap.create<Int, Word>()
  for (pair in data.wordOccurrences.asMap()) {
    val word = pair.key
    for (sentenceId in pair.value) {
      sentenceToWords.put(sentenceId.toInt(), word)
    }
  }

  val sentences = data.sentences.filter { it.language == Language.German }.mapIndexed { i, sentence -> i }
  val sentencesWithOptimalLength = sentences.filter { data.sentences[it].text.length > 50 && data.sentences[it].text.length < 150 }
  val sentenceFrequencies = sentencesWithOptimalLength.map { sentenceIndex ->
    val avg = sentenceToWords[sentenceIndex]!!.map { word ->
      data.wordFrequencies[word]!!
    }.average()
    Pair(sentenceIndex, avg)
  }

  val top = sentenceFrequencies.sortedByDescending { it.second }.take(10000)

  val resultSentences = top.map { it.first }

  return resultSentences
}

fun filterData(data: Data): Data {
  val filtered = filterGermanSentences(data)
  val sentences = mutableListOf<Sentence>()
  val translations = mutableMapOf<Long, Long>()

  for (sentenceIndex: Int in filtered) {
    val translatedIndex = data.sentenceTranslations[sentenceIndex.toLong()]!!.toInt()

    sentences.add(data.sentences[translatedIndex])
    sentences.add(data.sentences[sentenceIndex])

    val first = (sentences.size - 1).toLong()
    val second = (sentences.size - 2).toLong()

    translations.put(first, second)
    translations.put(second, first)
  }

  val occurrences = computeWordOccurrences(sentences)
  return Data(sentences, translations, occurrences, data.wordFrequencies)
}

fun computeFrequencies(corpusFrequencies: Map<Word, Long>, occurrences: Multimap<Word, Long>): Map<Word, Double> {
  val totalByLang = mutableMapOf<Language, Long>()
  val result = mutableMapOf<Word, Long>()

  for (occurrence in occurrences.asMap()) {
    val word = occurrence.key
    val corpusCount = corpusFrequencies[word] ?: 0L
    val totalCount = occurrence.value.size.toLong() + corpusCount
    totalByLang[word.language] = (totalByLang[word.language] ?: 0L) + totalCount
    result[word] = totalCount
  }

  return result.map { Pair(it.key, it.value.toDouble() / totalByLang[it.key.language]!!) }.toMap()
}

fun computeWordOccurrences(sentences: List<Sentence>): Multimap<Word, Long> {
  val result = LinkedHashMultimap.create<Word, Long>()

  sentences.forEachIndexed { i, sentence ->
    val language = sentence.language
    val text = sentence.text

    val words = splitWords(text)
        .filter {
          IntRange(it.start, it.end - 1).all { index ->
            text[index].isLetter()
          }
        }
        .map { text.substring(it.start, it.end).toLowerCase() }

    for (word in words) {
      result.put(Word(language, word), i.toLong())
    }
  }

  return result
}
