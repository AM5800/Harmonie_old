package am5800.common


data class LemmaOccurrence(val lemma: Lemma, val sentence: Sentence, val startIndex: Int, val endIndex: Int) {
  fun getForm(): String {
    return sentence.text.substring(startIndex, endIndex)
  }
}