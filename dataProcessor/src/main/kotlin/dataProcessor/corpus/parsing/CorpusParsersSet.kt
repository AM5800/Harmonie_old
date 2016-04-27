package dataProcessor.corpus.parsing

import dataProcessor.corpus.CorpusInfo
import java.util.*


class CorpusParsersSet {

  private val registeredParsers = ArrayList<CorpusParser>()

  fun registerParser(parser: CorpusParser) {
    registeredParsers.add(parser)
  }

  fun parse(info: CorpusInfo, vararg handlers: CorpusParserHandler) {
    val parser = registeredParsers.first { it.ParserId.equals(info.formatId, true) }
    parser.parse(info, MultiHandler(handlers))
  }

  fun parse(infos: Iterable<CorpusInfo>, vararg handlers: CorpusParserHandler) {
    for (info in infos) {
      val parser = registeredParsers.first { it.ParserId.equals(info.formatId, true) }
      parser.parse(info, MultiHandler(handlers))
    }
  }
}

