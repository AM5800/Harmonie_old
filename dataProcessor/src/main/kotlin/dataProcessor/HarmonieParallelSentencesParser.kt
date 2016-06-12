package dataProcessor

import am5800.common.LanguageParser
import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

class HarmonieParallelSentencesParser() {
  private class HarmonieParserHandler() : DefaultHandler() {
    fun getData(): Data {
      return Data(sentences, translations, occurrences.toList(), emptyMap(), emptyList(), occurrencePos)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
      if (qName == "ss") {
        if (sentencesInGroup.size > 1) {
          for (s1 in sentencesInGroup) {
            for (s2 in sentencesInGroup) {
              if (s1 === s2) continue

              translations.put(s1, s2)
              translations.put(s2, s1)
            }
          }
        }
        sentencesInGroup.clear()
      }
    }

    private val sentencesInGroup = mutableListOf<Sentence>()
    private val occurrencePos = mutableMapOf<WordOccurrence, PartOfSpeech>()
    private val occurrences = mutableSetOf<WordOccurrence>()
    private val translations = mutableMapOf<Sentence, Sentence>()
    private val sentences = mutableListOf<Sentence>()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "s") {
        val language = LanguageParser.parse(attributes.getValue("language"))
        val text = attributes.getValue("text")
        val sentence = Sentence(language, text)

        sentencesInGroup.add(sentence)
      } else if (qName == "w") {
        val lemma = attributes.getValue("lemma")!!
        val start = attributes.getValue("start").toInt()
        val end = attributes.getValue("end").toInt()
        val pos = parsePos(attributes.getValue("pos"))

        val sentence = sentencesInGroup.last()
        val word = Word(sentence.language, lemma)
        val occurrence = WordOccurrence(word, sentence, start, end)
        if (pos != PartOfSpeech.Other) occurrencePos.put(occurrence, pos)

        occurrences.add(occurrence)
      }
    }

    private fun parsePos(pos: String?): PartOfSpeech {
      try {
        return PartOfSpeech.valueOf(pos!!)
      } catch(e: Exception) {
        return PartOfSpeech.Other
      }
    }
  }

  fun parse(path: File): Data {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = HarmonieParserHandler()
    parser.parse(path, handler)
    return handler.getData()
  }
}