package dataProcessor.corpus.parsing

import dataProcessor.corpus.CorpusInfo


open class CorpusParserHandler {

  open fun beginCorpus(info: CorpusInfo) {
  }

  open fun beginSentence(id: String) {
  }

  open fun word(word: String, lemma: String, pos: ParsePartOfSpeech?) {
  }

  open fun endSentence() {
  }

  open fun endCorpus() {
  }
}