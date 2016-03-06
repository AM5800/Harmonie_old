import am5800.common.Language
import am5800.common.db.DbWordOccurrence
import am5800.common.db.Sentence
import am5800.common.db.Word
import corpus.CorpusRepository
import corpus.parsing.CorpusParsersSet
import corpus.parsing.NegraParser
import java.io.File

fun main(args: Array<String>) {
  val repository = CorpusRepository(File("data\\corpuses"))
  val parsersSet = CorpusParsersSet()
  parsersSet.registerParser(NegraParser())

  val data = prepareData(repository)
  val filteredData = filterData(data)

  DbWriter().write(File("androidApp\\src\\main\\assets\\content.db"), filteredData)
}

fun prepareData(repository: CorpusRepository): Data {
  val infos = repository.getCorpuses().filter { it.formatId.equals("parallel", true) }

  val sentences = mutableListOf<Sentence>()
  val translations = mutableMapOf<Sentence, Sentence>()
  val wordOccurrences = mutableListOf<DbWordOccurrence>()

  val parser = LetsmtCorpusParser()
  for (corpus in infos) {
    val enPath = File(corpus.infoFile.parentFile, corpus.metadata["en"])
    val dePath = File(corpus.infoFile.parentFile, corpus.metadata["de"])

    val enData = parser.parse(enPath)
    val deData = parser.parse(dePath)

    for (deSentencePair in deData.sentences) {
      val enSentence = enData.sentences[deSentencePair.key] ?: continue

      val englishSentence = Sentence(Language.English, enSentence)
      sentences.add(englishSentence)
      val germanSentence = Sentence(Language.German, deSentencePair.value)
      sentences.add(germanSentence)

      wordOccurrences.addAll(processWords(englishSentence, enData.words[deSentencePair.key]))
      wordOccurrences.addAll(processWords(germanSentence, deData.words[deSentencePair.key]))

      translations.put(englishSentence, germanSentence)
      translations.put(germanSentence, englishSentence)
    }
  }

  return Data(sentences, translations, wordOccurrences.distinct())
}

fun processWords(sentence: Sentence, occurrences: Set<WordOccurrence>): List<DbWordOccurrence> {
  return occurrences.map {
    val lemma = it.lemma
    val word = Word(sentence.language, lemma)
    DbWordOccurrence(word, sentence, it.sentenceStartIndex, it.sentenceEndIndex)
  }
}

class Data(val sentences: List<Sentence>,
           val sentenceTranslations: Map<Sentence, Sentence>,
           val wordOccurrences: List<DbWordOccurrence>)

fun filterGermanSentences(data: Data): List<Sentence> {
  val wordFrequencies: Map<Word, Double> = data.wordOccurrences
      .groupBy { it.word }
      .map { Pair(it.key, it.value.size.toDouble() / data.wordOccurrences.size) }
      .toMap()

  val sentenceToOccurrences = data.wordOccurrences.groupBy { it.sentence }

  val sentences = data.sentences.filter { it.language == Language.German }
  val sentencesWithOptimalLength = sentences.filter { it.text.length > 50 && it.text.length < 150 }

  val sentenceStats = sentencesWithOptimalLength.map { sentence ->
    val sentData = sentenceToOccurrences[sentence]!!.map { occurrence ->
      wordFrequencies[occurrence.word]!!
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

  val byBuckets = sortedBuckets.flatMap { it.second.take(it.second.size / 2) }.take(10000).map { it.first }

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
  val sentences = mutableListOf<Sentence>()
  val translations = mutableMapOf<Sentence, Sentence>()

  val oldOccurrences = data.wordOccurrences.groupBy { it.sentence }
  val wordOccurrences = mutableListOf<DbWordOccurrence>()

  for (sentence in filtered) {
    val translated = data.sentenceTranslations[sentence]!!

    sentences.add(sentence)
    sentences.add(translated)

    translations.put(sentence, translated)
    translations.put(translated, sentence)

    wordOccurrences.addAll(oldOccurrences[sentence]!!)
    wordOccurrences.addAll(oldOccurrences[translated]!!)
  }

  return Data(sentences, translations, wordOccurrences)
}

