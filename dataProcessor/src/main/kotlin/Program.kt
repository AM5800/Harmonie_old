import corpus.CorpusInfo
import corpus.CorpusRepository
import corpus.parsing.CorpusParsersSet
import corpus.parsing.NegraParser
import java.io.File

data class Sentence(val languages: String, val first: String, val second: String)

fun getParallelSentences(infos: List<CorpusInfo>): List<Sentence> {
  val sentences = mutableListOf<Sentence>()

  val sxmlCorpusParser = SxmlCorpusParser()
  for (corpus in infos) {
    val enPath = File(corpus.infoFile.parentFile, corpus.metadata["en"])
    val dePath = File(corpus.infoFile.parentFile, corpus.metadata["de"])
    val enData = sxmlCorpusParser.parse(enPath)
    val deData = sxmlCorpusParser.parse(dePath)

    val newSentences = enData.map { en ->
      val de = deData[en.key]!!
      Sentence("en-de", en.value, de)
    }

    sentences.addAll(newSentences)
  }

  return sentences
}

fun main(args: Array<String>) {
  val repository = CorpusRepository(File("data\\corpuses"))
  val parsersSet = CorpusParsersSet()
  parsersSet.registerParser(NegraParser())

  val sentences = getParallelSentences(repository.getCorpuses().filter { it.formatId.equals("parallel", true) })

  println(sentences.size)
}
