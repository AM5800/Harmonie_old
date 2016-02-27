import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import corpus.CorpusInfo
import corpus.CorpusRepository
import corpus.parsing.CorpusParsersSet
import corpus.parsing.NegraParser
import ml.sentenceBreaking.splitWords
import java.io.File
import java.util.*

fun computeParallelSentences(infos: List<CorpusInfo>, sentenceMinSize: Int = 50): Pair<Map<Long, Long>, List<Sentence>> {
  val sentences = mutableListOf<Sentence>()
  val translation = mutableMapOf<Long, Long>()

  val sxmlCorpusParser = SxmlCorpusParser()
  for (corpus in infos) {
    val enPath = File(corpus.infoFile.parentFile, corpus.metadata["en"])
    val dePath = File(corpus.infoFile.parentFile, corpus.metadata["de"])
    val enData = sxmlCorpusParser.parse(enPath)
    val deData = sxmlCorpusParser.parse(dePath)

    for (en in enData) {
      val de = deData[en.key]!!

      if (de.length < sentenceMinSize || en.value.length < sentenceMinSize) continue

      sentences.add(Sentence(Language.English, en.value))
      sentences.add(Sentence(Language.German, de))
      val gi = (sentences.size - 1).toLong()
      val ei = (sentences.size - 2).toLong()

      translation.put(gi, ei)
      translation.put(ei, gi)
    }
  }

  return Pair(translation, sentences)
}

fun main(args: Array<String>) {
  val repository = CorpusRepository(File("data\\corpuses"))
  val parsersSet = CorpusParsersSet()
  parsersSet.registerParser(NegraParser())

  val sentences = computeParallelSentences(repository.getCorpuses().filter { it.formatId.equals("parallel", true) })
  val occurrences = computeWordOccurrences(sentences.second)

  val frequencyHandler = WordsFrequencyHandler(occurrences.keySet())
  parsersSet.parse(repository.getCorpuses().filter { !it.formatId.equals("parallel", true) }, frequencyHandler)

  println("Sentences: ${sentences.second.size}")
  println("Unique words: ${occurrences.keySet().size}")
  val maxBy = frequencyHandler.result.maxBy { it.value }!!
  println("Top used word: '${maxBy.key}' with ${maxBy.value} usages")

  DbWriter().write(File("data\\db\\content.db"), sentences, occurrences, computeFrequencies(frequencyHandler.result, occurrences))
}

fun computeFrequencies(corpusFrequencies: LinkedHashMap<Word, Long>, occurrences: Multimap<Word, Long>): Map<Word, Double> {
  val totalByLang = mutableMapOf<Language, Long>()
  val result = mutableMapOf<Word, Long>()

  for (occurrence in occurrences.asMap()) {
    val word = occurrence.key
    val corpusCount = corpusFrequencies[word] ?: 0L
    val totalCount = occurrence.value.size.toLong() + corpusCount
    totalByLang[word.language] = totalByLang[word.language] ?: 0 + totalCount

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
