import am5800.common.LanguageParser
import am5800.common.db.Sentence
import am5800.common.db.Word
import am5800.common.db.WordOccurrence
import corpus.CorpusInfo
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

class HarmonieParallelSentencesParser {
  private class HarmonieParserHandler : DefaultHandler() {
    fun getData(): Data {
      return Data(translations, occurrences.toList(), emptyMap())
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
      if (qName == "sp") {
        assert(sentencesInGroup.size == 2)
        translations.put(sentencesInGroup[0], sentencesInGroup[1])
        translations.put(sentencesInGroup[1], sentencesInGroup[1])
        sentencesInGroup.clear()
      }
    }

    private val sentencesInGroup = mutableListOf<Sentence>()

    private val occurrences = mutableSetOf<WordOccurrence>()
    private val translations = mutableMapOf<Sentence, Sentence>()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "s") {
        val language = LanguageParser.parse(attributes.getValue("lang"))
        val text = attributes.getValue("text")

        sentencesInGroup.add(Sentence(language, text))
      } else if (qName == "w") {
        val lemma = attributes.getValue("lem")!!
        val start = attributes.getValue("start").toInt()
        val end = attributes.getValue("end").toInt()

        val sentence = sentencesInGroup.last()
        val language = sentence.language
        occurrences.add(WordOccurrence(Word(language, lemma), sentence, start, end))
      }
    }
  }


  fun parse(info: CorpusInfo): Data {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = HarmonieParserHandler()

    val relativePath = info.metadata["path"]!!
    val path = File(info.infoFile.parentFile, relativePath)
    parser.parse(path, handler)
    return handler.getData()
  }
}