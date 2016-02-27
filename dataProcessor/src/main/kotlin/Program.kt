import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import corpus.CorpusInfo
import corpus.CorpusRepository
import corpus.parsing.CorpusParsersSet
import corpus.parsing.NegraParser
import ml.sentenceBreaking.splitWords
import java.io.File

fun computeParallelSentences(infos: List<CorpusInfo>): List<SentenceGroup> {
  val sentences = mutableListOf<SentenceGroup>()

  val sxmlCorpusParser = SxmlCorpusParser()
  for (corpus in infos) {
    val enPath = File(corpus.infoFile.parentFile, corpus.metadata["en"])
    val dePath = File(corpus.infoFile.parentFile, corpus.metadata["de"])
    val enData = sxmlCorpusParser.parse(enPath)
    val deData = sxmlCorpusParser.parse(dePath)

    val newSentences = enData.map { en ->
      val de = deData[en.key]!!
      SentenceGroup.create {
        sentence(Language.English, en.value)
        sentence(Language.German, de)
      }
    }

    sentences.addAll(newSentences)
  }

  return sentences
}

fun main(args: Array<String>) {
  val repository = CorpusRepository(File("data\\corpuses"))
  val parsersSet = CorpusParsersSet()
  parsersSet.registerParser(NegraParser())

  val sentences = computeParallelSentences(repository.getCorpuses().filter { it.formatId.equals("parallel", true) })
  val occurrences = computeWordOccurrences(sentences)

  val frequencyHandler = WordsFrequencyHandler(occurrences.keySet())
  parsersSet.parse(repository.getCorpuses().filter { !it.formatId.equals("parallel", true) }, frequencyHandler)

  println("Sentences: ${sentences.size}")
  println("Unique words: ${occurrences.keySet().size}")
  val maxBy = frequencyHandler.result.maxBy { it.value }!!
  println("Top used word: '${maxBy.key}' with ${maxBy.value} usages")
}

fun computeWordOccurrences(sentences: List<SentenceGroup>): Multimap<String, Int> {
  val result = LinkedHashMultimap.create<String, Int>()

  sentences.forEachIndexed { i, sentenceGroup ->
    val german = sentenceGroup.byLang(Language.German) ?: return@forEachIndexed

    val words = splitWords(german)
        .filter {
          IntRange(it.start, it.end - 1).all { index ->
            german[index].isLetter()
          }
        }
        .map { german.substring(it.start, it.end).toLowerCase() }

    for (word in words) {
      result.put(word, i)
    }
  }

  return result
}