package dataProcessor.parsing

import am5800.common.CommonLemma
import am5800.common.Lemma
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory


class LemmasXmlParser {
  private class LemmasParserHandler() : DefaultHandler() {

    val lemmas = mutableListOf<Lemma>()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "l") {
        val id = attributes.getValue("id")!!
        val level = attributes.getValue("level")?.toInt()!!

        val lemma = CommonLemma.fromId(id, level)
        lemmas.add(lemma)
      }
    }
  }

  fun parse(path: File): List<Lemma> {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = LemmasParserHandler()
    parser.parse(path, handler)
    return handler.lemmas
  }
}