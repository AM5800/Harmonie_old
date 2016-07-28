package am5800.harmonie.app.model.sql

import am5800.common.CommonLemma


class SqlLemma(val sqlId: Long,
               lemmaId: String,
               difficultyLevel: Int) : CommonLemma(lemmaId, difficultyLevel) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false
    if (!super.equals(other)) return false

    other as SqlLemma

    if (sqlId != other.sqlId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + sqlId.hashCode()
    return result
  }
}