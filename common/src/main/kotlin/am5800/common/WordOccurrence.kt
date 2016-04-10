package am5800.common


data class WordOccurrence(val word: Word, val sentence: Sentence, val startIndex: Int, val endIndex: Int) {
  fun getForm(): String {
    return sentence.text.substring(startIndex, endIndex)
  }
}