package dataProcessor

import am5800.common.LanguageParser
import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

class HarmonieSentencesParser() {
  private class HarmonieParserHandler() : DefaultHandler(), ParseResult {
    private val _sentenceLevels = mutableMapOf<Sentence, Int>()
    override val sentenceLevels: Map<Sentence, Int>
      get() = _sentenceLevels

    private val _wordLevels = mutableMapOf<Word, Int>()
    override val wordLevels: Map<Word, Int>
      get() = _wordLevels

    override fun endElement(uri: String?, localName: String?, qName: String?) {
      if (qName == "ss") {
        if (sentencesInGroup.size > 1) {
          for (s1 in sentencesInGroup) {
            for (s2 in sentencesInGroup) {
              if (s1 === s2) continue

              _translations.put(s1, s2)
              _translations.put(s2, s1)
            }
          }
        }
        _sentences.addAll(sentencesInGroup)
        sentencesInGroup.clear()
      }
    }

    private val sentencesInGroup = mutableListOf<Sentence>()
    private val _occurrencePos = mutableMapOf<WordOccurrence, PartOfSpeech>()
    override val occurrencePos: Map<WordOccurrence, PartOfSpeech>
      get() = _occurrencePos

    private val _occurrences = mutableSetOf<WordOccurrence>()
    override val occurrences: Set<WordOccurrence>
      get() = _occurrences

    private val _translations = mutableMapOf<Sentence, Sentence>()
    override val translations: Map<Sentence, Sentence>
      get() = _translations

    private val _sentences = mutableListOf<Sentence>()

    override val sentences: List<Sentence>
      get() = _sentences

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "s") {
        val language = LanguageParser.parse(attributes.getValue("language"))
        val text = attributes.getValue("text")
        val level = attributes.getValue("level")?.toInt() ?: throw Exception("Unable to parse level: " + text)
        val sentence = Sentence(language, text)
        _sentenceLevels[sentence] = level

        sentencesInGroup.add(sentence)
      } else if (qName == "w") {
        val lemma = attributes.getValue("lemma")!!
        val start = attributes.getValue("start").toInt()
        val end = attributes.getValue("end").toInt()
        val pos = parsePos(attributes.getValue("pos"))
        val level = attributes.getValue("level").toInt()

        val sentence = sentencesInGroup.last()
        val word = Word(sentence.language, lemma)
        val occurrence = WordOccurrence(word, sentence, start, end)
        if (pos != PartOfSpeech.Other) _occurrencePos.put(occurrence, pos)

        _occurrences.add(occurrence)
        _wordLevels[word] = level
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

  fun parse(path: File): ParseResult {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = HarmonieParserHandler()
    parser.parse(path, handler)
    return handler
  }
}