package dataProcessor.german

import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class MorphyCsvParser(path: File) : GermanLemmatizer {
  override fun looksLikeSeparablePrefix(word: String): Boolean {
    val knownPrefixes = setOf("zu", "an")
    return knownPrefixes.contains(normalize(word))
  }

  override fun tryFindVerb(word: String, prefix: String): String? {
    val normalWord = normalize(word)
    val normalPrefix = normalize(prefix)

    val verb = normalPrefix + normalWord
    if (dictionary.containsKey(verb)) return verb
    return null
  }

  override fun tryFindLemma(form: String): String? {
    return dictionary[normalize(form)]
  }

  private val dictionary = parse(path)

  fun parse(path: File): Map<String, String> {
    BufferedReader(FileReader(path)).use { reader ->
      val result = mutableMapOf<String, String>()
      while (true) {
        val line = reader.readLine() ?: break
        if (line.startsWith('#')) continue
        val splitted = line.split('\t')
        assert(splitted.size == 2)

        val key = normalize(splitted[0])
        val value = normalize(splitted[1])

        result.put(key, value)
      }

      return result
    }
  }

  override fun normalize(lemma: String): String {
    return lemma.trim().toLowerCase().replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss")
  }
}