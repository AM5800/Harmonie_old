import com.google.common.collect.LinkedHashMultimap
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.util.*
import javax.xml.parsers.SAXParserFactory

data class SentenceId(val id: String)

data class WordOccurrence(val lemma: String, val sentenceStartIndex: Int, val sentenceEndIndex: Int)

class LetsmtCorpusParser {
  class LetsmtParserHandler() : DefaultHandler() {
    private class W(val tree: String, var lem: String, var word: String)


    private enum class Tags {
      S,
      W
    }

    val sentences = mutableMapOf<SentenceId, String>()
    val words = LinkedHashMultimap.create<SentenceId, WordOccurrence>()

    private val tagStack = Stack<Tags>()

    private var currentSentenceId: SentenceId? = null
    private val currentSentenceWords = mutableListOf<W>()

    override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {

      val tag = tryGetTag(qName) ?: return
      tagStack.push(tag)
      if (tag == Tags.S) currentSentenceId = SentenceId(attributes.getValue("id"))
      else if (tag == Tags.W) {
        val tree = attributes.getValue("tree") ?: return
        val lem = processLem(attributes.getValue("lem"))

        currentSentenceWords.add(W(tree, lem.toLowerCase(), ""))
      }
    }

    private fun processLem(value: String?): String {
      if (value == null) return ""
      return value.split('|').first()
    }

    private fun tryGetTag(qName: String): Tags? {
      return Tags.values().singleOrNull { it.name.toLowerCase() == qName }
    }

    override fun endElement(uri: String?, localName: String?, qName: String) {
      val tag = tryGetTag(qName) ?: return
      if (tagStack.last() != tag) throw Exception("Unexpected state")
      tagStack.pop()

      if (tag == Tags.S) {
        composeSentence()
        currentSentenceId = null
        currentSentenceWords.clear()
      }
    }

    private fun composeSentence() {
      if (currentSentenceWords.all { !it.tree.startsWith("V") }) {
        // We don't need sentences without verbs
        return
      }

      processVerbzusatz()
      val sentenceId = currentSentenceId!!
      val builder = StringBuilder()
      for (word in currentSentenceWords) {
        val needSpace = needSpace(builder, word.word)
        if (needSpace) builder.append(' ')

        val start = builder.length
        builder.append(word.word)
        val end = builder.length
        if (word.lem.isBlank()) word.lem = word.word.toLowerCase()
        if (accept(word)) words.put(sentenceId, WordOccurrence(word.lem, start, end))
      }

      sentences.put(sentenceId, builder.toString())
    }

    private fun accept(word: W): Boolean {
      if (word.word.any { !it.isLetter() }) return false
      return when (word.tree.toLowerCase()) {
        "np" -> false
        "card" -> false
        "$(", "$,", "$." -> false
        "--" -> false
        "fm" -> false
        "xy" -> false
        else -> true
      }
    }

    private fun needSpace(builder: StringBuilder, word: String): Boolean {
      if (builder.length == 0) return false
      val needSpaceAfterButNotInFront = "?,.!".toCharArray().toSet()
      val neverNeedSpace = "_/\\".toCharArray().toSet()


      if (needSpaceAfterButNotInFront.contains(builder.last())) return true
      if (needSpaceAfterButNotInFront.contains(word.first())) return false
      if (neverNeedSpace.contains(builder.last())) return false
      if (neverNeedSpace.contains(word.first())) return false

      return true
    }

    private fun processVerbzusatz() {
      var lastVerb: W? = null
      for (word in currentSentenceWords) {
        if (word.tree.startsWith("V")) lastVerb = word
        if (word.tree == "PTKVZ") {
          if (lastVerb == null) {
            println("No suitable verb found in " + currentSentenceWords.map { it.word.toString() + "(${it.tree})" }.joinToString(" "))
            continue
          }

          val newLemma = word.lem + lastVerb.lem
          lastVerb.lem = newLemma
          word.lem = newLemma
        }
      }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
      if (tagStack.isEmpty()) return
      val tag = tagStack.last()

      if (tag == Tags.W && currentSentenceWords.any()) {
        currentSentenceWords.last().word = String(ch, start, length)
      }
    }
  }

  fun parse(file: File): LetsmtParserHandler {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val handler = LetsmtParserHandler()
    parser.parse(file, handler)
    return handler
  }
}