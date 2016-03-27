package am5800.common.db

import am5800.common.Language
import am5800.common.code

open class Word(val language: Language, val lemma: String) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as Word

    if (language != other.language) return false
    if (lemma != other.lemma) return false

    return true
  }

  override fun hashCode(): Int {
    var result = language.hashCode()
    result += 31 * result + lemma.hashCode()
    return result
  }

  override fun toString(): String {
    return "${language.code()}: $lemma"
  }
}