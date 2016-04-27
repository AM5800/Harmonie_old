package dataProcessor

import com.google.common.collect.LinkedHashMultimap
import dataProcessor.corpus.parsing.CorpusParserHandler
import dataProcessor.corpus.parsing.ParsePartOfSpeech

class LemmaDisambiguationCounter : CorpusParserHandler() {
  val result = LinkedHashMultimap.create<String, ParsePartOfSpeech>()

  override fun word(word: String, lemma: String, pos: ParsePartOfSpeech?) {
    if (pos != null && pos != ParsePartOfSpeech.ProperName) result.put(lemma, pos)
  }

  var i = 1
  fun print() {
    for ((lemma, poss) in result.asMap()) {
      if (poss.size <= 1 ) continue
      println("$i: $lemma: " + poss.joinToString(", ") { it.toString() })
      ++i
    }
  }
}