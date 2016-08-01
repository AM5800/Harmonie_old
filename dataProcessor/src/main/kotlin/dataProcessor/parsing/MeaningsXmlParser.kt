package dataProcessor.parsing

import am5800.common.Language
import am5800.common.Lemma
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.SAXParserFactory

class MeaningsParseResult(val lemmasLanguage: Language, val meaningsLanguage: Language, val meanings: Multimap<Lemma, String>)

class MeaningsXmlParser {
  private class MeaningsParserHandler(private val idToLemma: Map<String, Lemma>) : DefaultHandler() {

    private val meaningsMap = LinkedHashMultimap.create<Lemma, String>()
    private var lemmasLanguage: Language? = null
    private var meaningsLanguage: Language? = null

    val result: MeaningsParseResult
      get() = MeaningsParseResult(lemmasLanguage!!, meaningsLanguage!!, meaningsMap)

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {
      if (qName == "m") {
        val lemmaId = attributes.getValue("lemma")!!

        val lemma = idToLemma[lemmaId] ?: return

        val meanings = attributes.getValue("meanings")!!.split(';').map { it.trim() }

        meaningsMap.putAll(lemma, meanings)
      } else if (qName == "ms") {
        lemmasLanguage = Language.parse(attributes.getValue("lemmasLanguage"))
        meaningsLanguage = Language.parse(attributes.getValue("meaningsLanguage"))
      }
    }
  }

  fun parse(path: File, idToLemma: Map<String, Lemma>): MeaningsParseResult {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = MeaningsParserHandler(idToLemma)
    parser.parse(path, handler)
    return handler.result
  }
}

fun MeaningsParseResult.merge(other: MeaningsParseResult) : MeaningsParseResult {
  assert(this.lemmasLanguage == other.lemmasLanguage)
  assert(this.meaningsLanguage == other.meaningsLanguage)

  this.meanings.putAll(other.meanings)
  return this
}

fun List<MeaningsParseResult>.mergeByLanguages(): List<MeaningsParseResult> {
  val map = mutableMapOf<Pair<Language, Language>, MeaningsParseResult>()

  for (mpr in this) {
    val languagePair = Pair(mpr.lemmasLanguage, mpr.lemmasLanguage)
    val prev = map[languagePair]
    if (prev == null) map[languagePair] = mpr
    else map[languagePair] = prev.merge(mpr)
  }

  return map.map { it.value }
}