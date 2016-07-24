package am5800.harmonie.app.model.services

import am5800.common.CommonLemma
import am5800.common.Language
import am5800.common.PartOfSpeech


class SqlLemma(val sqlId: Long,
               language: Language,
               lemma: String,
               difficultyLevel: Int,
               partOfSpeech: PartOfSpeech) : CommonLemma(lemma, language, partOfSpeech, difficultyLevel) {
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