package dataProcessor.corpus.parsing.shuffling

import am5800.common.utils.functions.shuffle
import dataProcessor.corpus.CorpusInfo
import dataProcessor.corpus.CorpusRepository
import dataProcessor.corpus.parsing.CorpusParserHandler
import dataProcessor.corpus.parsing.CorpusParsersSet
import dataProcessor.corpus.parsing.SentenceSelector
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.util.*
import java.util.logging.Logger


class CorpusShuffleRepository(private val repo: CorpusRepository,
                              private val parsers: CorpusParsersSet,
                              private val logger: Logger) {

  fun shuffleIfNeeded(selector: SentenceSelector) {
    for (info in repo.getCorpuses()) {
      val shuffleFilePath = getShufflePath(info) // TODO better error handling
      if (shuffleFilePath.exists()) continue

      logger.info("Shuffling $shuffleFilePath")

      selector.clear()
      parsers.parse(info, selector)
      val ids = selector.getIds()

      val shuffled = ids.shuffle()

      PrintWriter(shuffleFilePath, "UTF-8").use { writer ->
        writer.println(shuffled.size)
        shuffled.forEach { writer.println(it) }
      }

      logger.info("Shuffle done")
    }
  }

  private fun getShufflePath(corpusInfo: CorpusInfo): File {
    return File(corpusInfo.infoFile.parentFile, corpusInfo.metadata["shuffleFile"]!!)
  }

  fun newHandler(handler: CorpusParserHandler, range: RelativeRange): CorpusParserHandler {
    return ReadCorpusShuffleHandler(this, handler, range)
  }

  fun getShuffle(info: CorpusInfo): HashMap<String, Int>? {
    val shuffleFile = getShufflePath(info)
    return loadShuffleInfo(shuffleFile)
  }

  private fun loadShuffleInfo(shuffleFile: File): HashMap<String, Int> {
    val result = LinkedHashMap<String, Int>()
    BufferedReader(FileReader(shuffleFile)).use { reader ->
      val n = Integer.parseInt(reader.readLine())
      repeat(n, { i ->
        val line = reader.readLine()?.trim()
        if (line != null) result.put(line, i)
      })
    }
    return result
  }
}