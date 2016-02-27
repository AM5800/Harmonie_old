import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

data class SentenceId(val id: String)


class SxmlCorpusParser {
  private class SxmlParserHandler() : DefaultHandler() {
    val result = mutableMapOf<SentenceId, String>()

    var currentSentence: SentenceId? = null
    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "s") {
        currentSentence = SentenceId(attributes.getValue("id"))
      }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
      if (qName == "s") {
        currentSentence = null
      }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
      val sentenceId = currentSentence ?: return
      val sentence = String(ch, start, length)

      result.put(sentenceId, sentence)
    }
  }


  fun parse(file: File): Map<SentenceId, String> {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = SxmlParserHandler()
    parser.parse(file, handler)
    return handler.result
  }
}