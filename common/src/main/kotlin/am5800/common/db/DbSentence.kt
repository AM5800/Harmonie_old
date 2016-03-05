package am5800.common.db

import am5800.common.Language

open class DbSentence(val language: Language, val text: String) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as DbSentence

    if (language != other.language) return false
    if (text != other.text) return false

    return true
  }

  override fun hashCode(): Int {
    var result = language.hashCode()
    result += 31 * result + text.hashCode()
    return result
  }
}