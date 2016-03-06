package am5800.common.db

import am5800.common.Language

open class Word(val language: Language, val word: String) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as Word

    if (language != other.language) return false
    if (word != other.word) return false

    return true
  }

  override fun hashCode(): Int {
    var result = language.hashCode()
    result += 31 * result + word.hashCode()
    return result
  }
}