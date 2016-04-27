package dataProcessor.corpus.parsing.shuffling

import dataProcessor.corpus.CorpusInfo
import dataProcessor.corpus.parsing.CorpusParserHandler
import dataProcessor.corpus.parsing.ParsePartOfSpeech
import java.util.*


class ReadCorpusShuffleHandler(private val repository: CorpusShuffleRepository,
                               private val original: CorpusParserHandler,
                               private val range: RelativeRange) : CorpusParserHandler() {
  private var shuffle: HashMap<String, Int>? = null
  private var skip = false
  private var currentSentence: String = ""

  override fun beginCorpus(info: CorpusInfo) {
    shuffle = repository.getShuffle(info)
  }

  override fun beginSentence(id: String) {
    currentSentence = id
    val shuffle = this.shuffle!!
    val index = shuffle[id]
    skip = index == null || !range.accept(index, shuffle.size)

    if (!skip) {
      original.beginSentence(id)
    }
  }

  override fun word(word: String, lemma: String, pos: ParsePartOfSpeech?) {
    if (!skip) {
      original.word(word, lemma, pos)
    }
  }

  override fun endSentence() {
    if (!skip) {
      original.endSentence()
    }
  }

  override fun endCorpus() {
    if (!skip) {
      original.endCorpus()
    }
  }
}