package am5800.common.db

import am5800.common.Language


class SqlWord(val id: Long, language: Language, word: String) : DbWord(language, word) {
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