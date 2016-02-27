import corpus.CorpusInfo
import corpus.parsing.CorpusParserHandler
import corpus.parsing.ParsePartOfSpeech
import java.util.*

class WordsFrequencyHandler(words: Set<Word>) : CorpusParserHandler() {
  val result = LinkedHashMap<Word, Long>()
  var currentLanguage: Language? = null

  init {
    result.putAll (words.map { Pair(it, 0L) })
  }

  override fun beginCorpus(info: CorpusInfo) {
    currentLanguage = LanguageParser.parse(info.metadata["lang"]!!)
  }

  override fun word(word: String, lemma: String, pos: ParsePartOfSpeech?) {
    val lang = currentLanguage ?: return
    val key = Word(lang, word.toLowerCase())
    val value = result[key] ?: return
    result[key] = value + 1
  }
}