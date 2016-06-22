package am5800.harmonie.app.model.services

import am5800.common.Language
import am5800.common.Word


class SqlWord(val id: Long, language: Language, lemma: String) : Word(language, lemma) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    if (!super.equals(other)) return false

    other as SqlWord

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result += 31 * result + id.hashCode()
    return result
  }
}