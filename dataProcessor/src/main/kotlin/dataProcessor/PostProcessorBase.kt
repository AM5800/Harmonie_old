package dataProcessor

abstract class PostProcessorBase : SentencePostProcessor {
  protected fun isLemmatized(metadata: Map<String, String>): Boolean {
    val value = metadata["lemmatized"] ?: return false
    return value.toBoolean()
  }

  protected fun shouldDelete(occurrence: ParseWordOccurrence): Boolean {
    if (occurrence.lemma.any { c -> c.isDigit() }) return true
    if (isPunctuation(occurrence.lemma)) return true

    return false
  }

  protected fun isPunctuation(s: String): Boolean {
    val chars = "[,.'\"'`!?<>{}():]-".toCharArray().toSet()
    return s.any() && s.all { c -> chars.contains(c) }
  }
}