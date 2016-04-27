package dataProcessor.corpus.parsing

import dataProcessor.corpus.CorpusInfo

class MultiHandler(private val handlers: Array<out CorpusParserHandler>) : CorpusParserHandler() {
  override fun beginCorpus(info: CorpusInfo) {
    handlers.forEach { it.beginCorpus(info) }
  }

  override fun beginSentence(id: String) {
    handlers.forEach { it.beginSentence(id) }
  }

  override fun word(word: String, lemma: String, pos: ParsePartOfSpeech?) {
    handlers.forEach { it.word(word, lemma, pos) }
  }

  override fun endSentence() {
    handlers.forEach { it.endSentence() }
  }

  override fun endCorpus() {
    handlers.forEach { it.endCorpus() }
  }
}