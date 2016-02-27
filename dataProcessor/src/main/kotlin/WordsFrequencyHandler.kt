import corpus.parsing.CorpusParserHandler
import corpus.parsing.ParsePartOfSpeech
import java.util.*

class WordsFrequencyHandler(words: Set<String>) : CorpusParserHandler() {
  val result = LinkedHashMap<String, Int>()

  init {
    result.putAll (words.map { Pair(it, 0) })
  }

  override fun word(word: String, lemma: String, pos: ParsePartOfSpeech?) {
    val key = word.toLowerCase()
    val value = result[key] ?: return
    result[key] = value + 1
  }
}