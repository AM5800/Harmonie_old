package am5800.harmonie.app.model.dbAccess.sql

import am5800.common.Language
import am5800.common.db.Sentence


class SqlSentence(val id: Long, language: Language, text: String) : Sentence(language, text) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    if (!super.equals(other)) return false

    other as SqlSentence

    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result += 31 * result + id.hashCode()
    return result
  }
}