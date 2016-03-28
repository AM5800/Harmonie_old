package dataProcessor

import am5800.common.LanguageParser
import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence
import corpus.CorpusInfo
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

class HarmonieParallelSentencesParser(private val postProcessors: List<SentencePostProcessor>) {
  private class HarmonieParserHandler(private val postProcessors: List<SentencePostProcessor>, private val info: CorpusInfo) : DefaultHandler() {
    fun getData(): Data {
      return Data(translations, occurrences.toList(), emptyMap(), emptyMap())
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
      if (qName == "s") {
        assert(sentencesInGroup.size >= 1)
        val sentence = sentencesInGroup.last()
        val language = sentence.language
        val postProcessor = postProcessors.firstOrNull { it.language == language }
        postProcessor?.processInPlace(currentSentenceOccurrences, info.metadata)

        occurrences.addAll(currentSentenceOccurrences.map { WordOccurrence(Word(language, it.lemma), sentence, it.start, it.end) })
        currentSentenceOccurrences.clear()
      }
      if (qName == "sp") {
        assert(sentencesInGroup.size == 2)
        translations.put(sentencesInGroup[0], sentencesInGroup[1])
        translations.put(sentencesInGroup[1], sentencesInGroup[0])
        sentencesInGroup.clear()
      }
    }

    private val sentencesInGroup = mutableListOf<Sentence>()

    private val occurrences = mutableSetOf<WordOccurrence>()
    private val translations = mutableMapOf<Sentence, Sentence>()
    private val currentSentenceOccurrences = mutableListOf<ParseWordOccurrence>()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "s") {
        val language = LanguageParser.parse(attributes.getValue("lang"))
        val text = attributes.getValue("text")

        sentencesInGroup.add(Sentence(language, text))
        currentSentenceOccurrences.clear()
      } else if (qName == "w") {
        val lemma = attributes.getValue("lem")!!
        val start = attributes.getValue("start").toInt()
        val end = attributes.getValue("end").toInt()

        currentSentenceOccurrences.add(ParseWordOccurrence(lemma, start, end))
      }
    }
  }


  fun parse(info: CorpusInfo): Data {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = HarmonieParserHandler(postProcessors, info)

    val relativePath = info.metadata["path"]!!
    val path = File(info.infoFile.parentFile, relativePath)
    parser.parse(path, handler)
    return handler.getData()
  }
}