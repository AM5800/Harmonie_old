package am5800.common

open class CommonLemma private constructor(override val id: String,
                                           override val lemma: String,
                                           override val language: Language,
                                           override val partOfSpeech: PartOfSpeech,
                                           override val difficultyLevel: Int) : Lemma {

  constructor(lemma: String, language: Language, partOfSpeech: PartOfSpeech, difficultyLevel: Int) : this(mkId(lemma, language, partOfSpeech), lemma, language, partOfSpeech, difficultyLevel)

  companion object {
    fun fromId(id: String, level: Int): Lemma {
      val chunks = id.split(':')
      if (chunks.size != 3) throw Exception("Unexpected id: " + id)
      val language = Language.parse(chunks[0])
      val lemma = chunks[1]
      val pos = PartOfSpeech.parse(chunks[2])

      return CommonLemma(id, lemma, language, pos, level)
    }

    private fun mkId(lemma: String, language: Language, partOfSpeech: PartOfSpeech): String {
      return "${language.code}:lemma:$partOfSpeech"
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as CommonLemma

    if (id != other.id) return false
    if (lemma != other.lemma) return false
    if (language != other.language) return false
    if (partOfSpeech != other.partOfSpeech) return false
    if (difficultyLevel != other.difficultyLevel) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + lemma.hashCode()
    result = 31 * result + language.hashCode()
    result = 31 * result + partOfSpeech.hashCode()
    result = 31 * result + difficultyLevel
    return result
  }
}