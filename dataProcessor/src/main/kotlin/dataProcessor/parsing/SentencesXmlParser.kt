package dataProcessor.parsing

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.LemmaOccurrence
import am5800.common.Sentence
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

class SentencesParseResult(val sentences: List<Sentence>, val occurrences: List<LemmaOccurrence>)

fun List<SentencesParseResult>.merge(): SentencesParseResult {
  val allSentences = this.flatMap { it.sentences }.distinct()
  val allOccurrences = this.flatMap { it.occurrences }.distinct()

  return SentencesParseResult(allSentences, allOccurrences)
}

class SentencesXmlParser() {
  private class SentencesParserHandler(lemmas: List<Lemma>) : DefaultHandler() {

    private val lemmaIdToLemma = lemmas.map { Pair(it.id, it) }.toMap()

    val sentences = mutableListOf<Sentence>()
    val occurrences = mutableListOf<LemmaOccurrence>()

    private var language: Language? = null

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "ss") {
        language = Language.parse(attributes.getValue("language"))
      } else if (qName == "s") {
        val text = attributes.getValue("text")
        val level = attributes.getValue("level")?.toInt()
        val uid = attributes.getValue("id")
        val sentence = Sentence(uid, language!!, text, level)
        sentences.add(sentence)
      } else if (qName == "o") {
        val lemmaId = attributes.getValue("lemmaId")!!
        val start = attributes.getValue("start").toInt()
        val end = attributes.getValue("end").toInt()

        val sentence = sentences.last()
        val lemma = lemmaIdToLemma[lemmaId] ?: return
        val occurrence = LemmaOccurrence(lemma, sentence, start, end)
        occurrences.add(occurrence)
      }
    }
  }

  fun parse(path: File, lemmas: List<Lemma>): SentencesParseResult {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = SentencesParserHandler(lemmas)
    parser.parse(path, handler)
    return SentencesParseResult(handler.sentences, handler.occurrences)
  }
}