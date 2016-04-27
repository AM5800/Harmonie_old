package dataProcessor.corpus.parsing

import dataProcessor.corpus.CorpusInfo
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory


class NegraParser : CorpusParser {
  override val ParserId: String = "NEGRA4"

  private class SaxHandler(private val handler: CorpusParserHandler) : DefaultHandler() {

    private var inSentence = false

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
      if (qName == "s") {
        val id = attributes?.getValue("id")!!.trim()
        handler.beginSentence(id)
        inSentence = true
      }
      if (inSentence && qName == "t") {
        processWord(attributes)
      }

      super.startElement(uri, localName, qName, attributes)
    }

    private fun processWord(attributes: Attributes?) {
      val word = attributes?.getValue("word")!!
      val lemma = attributes?.getValue("lemma")!!
      val pos = attributes?.getValue("pos")

      if (!word.trimEnd('.').isEmpty() && word.last() == '.') {
        handler.word(word.trimEnd('.'), lemma.trimEnd('.'), getPos(pos, word))
        handler.word(".", ".", ParsePartOfSpeech.Punctuation)
      } else handler.word(word, lemma, getPos(pos, word))
    }

    private fun getPos(pos: String?, word: String): ParsePartOfSpeech? {
      if (word == "``") return ParsePartOfSpeech.QuotationStart
      else if (word == "''") return ParsePartOfSpeech.QuotationEnd

      return when (pos) {
      // TODO more mapping!
        "ADJA", "ADJV" -> ParsePartOfSpeech.Adjective
        "ADV" -> ParsePartOfSpeech.Adverb
        "CARD" -> ParsePartOfSpeech.Number
        "FM" -> ParsePartOfSpeech.ForeignLanguageMaterial
        "NE" -> ParsePartOfSpeech.ProperName
        "NN" -> ParsePartOfSpeech.Noun
        "VAFIN", "VAIMP", "VAINF", "VVPP", "VVFIN",
        "VVIMP", "VVINF", "VVIZU", "VVPP" -> ParsePartOfSpeech.Verb
        "ART" -> ParsePartOfSpeech.Determiner
        "$.", "$,", "$(" -> ParsePartOfSpeech.Punctuation

        else -> null
      }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
      if (qName == "s") {
        handler.endSentence()
        inSentence = false
      }

      super.endElement(uri, localName, qName)
    }
  }

  override fun parse(info: CorpusInfo, handler: CorpusParserHandler) {
    assert(info.formatId == ParserId)

    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    handler.beginCorpus(info)
    val path = File(info.infoFile.parentFile, info.metadata["path"])
    parser.parse(path, SaxHandler(handler))
    handler.endCorpus()
  }
}


