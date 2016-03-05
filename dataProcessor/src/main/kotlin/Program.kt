import am5800.common.Language
import am5800.common.db.SQLSentence
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import corpus.CorpusInfo
import corpus.CorpusRepository
import corpus.parsing.CorpusParsersSet
import corpus.parsing.NegraParser
import ml.sentenceBreaking.splitWords
import java.io.File

fun computeParallelSentences(infos: List<CorpusInfo>): Pair<Map<Long, Long>, List<SQLSentence>> {
  val sentences = mutableListOf<SQLSentence>()
  val translations = mutableMapOf<Long, Long>()

  val sxmlCorpusParser = SxmlCorpusParser()
  var id = 0L
  for (corpus in infos) {
    val enPath = File(corpus.infoFile.parentFile, corpus.metadata["en"])
    val dePath = File(corpus.infoFile.parentFile, corpus.metadata["de"])
    val enData = sxmlCorpusParser.parse(enPath)
    val deData = sxmlCorpusParser.parse(dePath)

    for (de in deData) {
      val en = enData[de.key] ?: continue

      val firstId = id++
      val secondId = id++
      sentences.add(SQLSentence(firstId, Language.English, en))
      sentences.add(SQLSentence(secondId, Language.German, de.value))

      translations.put(firstId, secondId)
      translations.put(secondId, firstId)
    }
  }

  return Pair(translations, sentences)
}

class Data(val sentences: List<SQLSentence>,
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

fun filterGermanSentences(data: Data): List<SQLSentence> {
  val sentenceToWords = LinkedHashMultimap.create<Long, Word>()
  for (pair in data.wordOccurrences.asMap()) {
    val word = pair.key
    for (sentenceId in pair.value) {
      sentenceToWords.put(sentenceId, word)
    }
  }

  val sentences = data.sentences.filter { it.language == Language.German }
  val sentencesWithOptimalLength = sentences.filter { it.text.length > 50 && it.text.length < 150 }

  val sentenceStats = sentencesWithOptimalLength.map { sentence ->
    val sentData = sentenceToWords[sentence.id]!!.map { word ->
      data.wordFrequencies[word]!!
    }.computeSentenceData()
    Pair(sentence, sentData)
  }


  val minFrequency = sentenceStats.map { it.second.expectation }.min()!!
  val maxFrequency = sentenceStats.map { it.second.expectation }.max()!!
  val bucketsN = 1000
  val bucketSize = (maxFrequency - minFrequency) / bucketsN

  val buckets = sentenceStats.groupBy { (it.second.expectation / bucketSize).toInt() }
  val sortedBuckets = buckets.map { Pair(it.key, it.value.sortedBy { s -> s.second.dispersion }) }
  println("No of buckets: " + buckets.count())

  val byExpectation = sentenceStats.sortedByDescending { it.second.expectation }
  val byDispersion = sentenceStats.sortedBy { it.second.dispersion }
  val byBuckets = sortedBuckets.flatMap { it.second.take(it.second.size / 2) }.take(10000).map { it.first }


  println("byExpectation: " + byExpectation.take(5).map { it.first }.joinToString("\n"))
  println("byDispersion: " + byDispersion.take(50).map { it.first }.joinToString("\n"))

  return byBuckets
}

data class StatData(val expectation: Double, val dispersion: Double)

fun List<Double>.computeSentenceData(): StatData {
  val avg = this.average()
  val dispersion = this.map { it - avg }.map { it * it }.sum() / this.count()

  return StatData(avg, dispersion)
}

fun filterData(data: Data): Data {
  val filtered = filterGermanSentences(data)
  val sentences = mutableListOf<SQLSentence>()
  val translations = mutableMapOf<Long, Long>()

  val sentencesMap = data.sentences.map { Pair(it.id, it) }.toMap()

  var id = 0L
  for (sentence in filtered) {
    val translated = sentencesMap[data.sentenceTranslations[sentence.id]!!]!!

    val firstId = id++
    val newSentence = SQLSentence(firstId, sentence.language, sentence.text)

    val secondId = id++
    val newTranslatedSentence = SQLSentence(secondId, translated.language, translated.text)
    sentences.add(newSentence)
    sentences.add(newTranslatedSentence)

    translations.put(firstId, secondId)
    translations.put(secondId, firstId)
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

fun computeWordOccurrences(sentences: List<SQLSentence>): Multimap<Word, Long> {
  val result = LinkedHashMultimap.create<Word, Long>()

  sentences.forEach { sentence ->
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
      result.put(Word(language, word), sentence.id)
    }
  }

  return result
}
