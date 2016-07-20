package dataProcessor

import am5800.common.Language
import am5800.common.Word
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory


class CountsParser {
  class CountsParserHandler : DefaultHandler() {
    private val _result = mutableMapOf<Word, Int>()
    val result: Map<Word, Int>
      get() = _result

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "lemma") {
        val lemma = attributes.getValue("value")!!
        val language = Language.parse(attributes.getValue("language"))
        val count = Integer.parseInt(attributes.getValue("count"))

        _result.put(Word(language, lemma), count)
      }
    }
  }

  fun parse(path: File): Map<Word, Int> {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = CountsParserHandler()
    parser.parse(path, handler)
    return handler.result
  }
}

