package dataProcessor.corpus.parsing

import dataProcessor.corpus.CorpusInfo


interface CorpusParser {
  val ParserId: String
  fun parse(info: CorpusInfo, handler: CorpusParserHandler)
}