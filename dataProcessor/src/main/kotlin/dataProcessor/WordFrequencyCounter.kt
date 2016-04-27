package dataProcessor

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.Word
import dataProcessor.corpus.CorpusInfo
import dataProcessor.corpus.parsing.CorpusParserHandler
import dataProcessor.corpus.parsing.ParsePartOfSpeech

class WordFrequencyCounter(private val postProcessors: List<SentencePostProcessor>, data: Data) : CorpusParserHandler() {
  private val occurrences = mutableListOf<ParseWordOccurrence>()
  private var currentPostProcessor: SentencePostProcessor? = null
  val wordCounts = mutableMapOf<Word, Int>()
  var language: Language? = null

  init {
    val dataCounts = data.wordOccurrences.groupBy { it.word }.mapValues { it.value.count() }
    wordCounts.putAll(dataCounts)
  }

  private var currentInfo: CorpusInfo? = null

  override fun beginCorpus(info: CorpusInfo) {
    val langCode = info.metadata["language"] ?: throw Exception("Language not set for corpus: " + info.infoFile.absolutePath)
    language = LanguageParser.parse(langCode)
    currentInfo = info

    currentPostProcessor = postProcessors.firstOrNull { it.language == language }
  }

  override fun endSentence() {
    currentPostProcessor?.processInPlace(occurrences, currentInfo!!.metadata)
    for (occurrence in occurrences) {
      val word = Word(language!!, occurrence.lemma)

      if (!wordCounts.contains(word)) continue
      wordCounts[word] = wordCounts[word]!! + 1
    }
    occurrences.clear()
  }

  override fun word(word: String, lemma: String, pos: ParsePartOfSpeech?) {
    occurrences.add(ParseWordOccurrence(lemma, -1, -1))
  }
}