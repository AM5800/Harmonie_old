package dataProcessor.corpus.parsing

abstract class SentenceSelector : CorpusParserHandler() {
  abstract fun getIds(): List<String>

  abstract fun clear()
}

class OnlyPlainSentenceSelector : SentenceSelector() {
  private val ids = mutableSetOf<String>()

  override fun clear() {
    ids.clear()
  }

  private var currentSentence: String? = null

  override fun beginSentence(id: String) {
    currentSentence = id
  }

  override fun word(word: String, lemma: String, pos: ParsePartOfSpeech?) {
    if (currentSentence == null) return
    if (word.indexOfAny("'\"`():/;_".toCharArray()) != -1) currentSentence = null
  }

  override fun endSentence() {
    val current = currentSentence ?: return
    ids.add(current)
  }

  override fun getIds(): List<String> {
    return ids.toList()
  }
}