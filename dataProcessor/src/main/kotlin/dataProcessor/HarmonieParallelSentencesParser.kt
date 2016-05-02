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
      return Data(translations, occurrences.toList(), difficulties, emptyMap(), emptyList(), occurrencePos)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
      if (qName == "sp") {
        assert(sentencesInGroup.size == 2)
        translations.put(sentencesInGroup[0], sentencesInGroup[1])
        translations.put(sentencesInGroup[1], sentencesInGroup[0])
        sentencesInGroup.clear()
      }
    }

    private val sentencesInGroup = mutableListOf<Sentence>()

    private val difficulties = mutableMapOf<Sentence, Int>()
    private val occurrencePos = mutableMapOf<WordOccurrence, PartOfSpeech>()
    private val occurrences = mutableSetOf<WordOccurrence>()
    private val translations = mutableMapOf<Sentence, Sentence>()

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "s") {
        val language = LanguageParser.parse(attributes.getValue("language"))
        val text = attributes.getValue("text")
        val difficulty = Integer.parseInt(attributes.getValue("difficulty") ?: "-1")

        val sentence = Sentence(language, text)

        if (difficulty != -1) difficulties.put(sentence, difficulty)

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