package am5800.common

open class CommonLemma(override val id: String,
                       override val difficultyLevel: Int) : Lemma {
  override val lemma: String
    get() = getChunks(1)
  override val language: Language
    get() = Language.parse(getChunks(0))
  override val partOfSpeech: PartOfSpeech
    get() = PartOfSpeech.parse(getChunks(2))

  private fun getChunks(index: Int): String {
    val chunks = id.split(':')
    if (chunks.size != 3) throw Exception("Unexpected id: " + id)
    return chunks[index]
  }

  constructor(lemma: String, language: Language, partOfSpeech: PartOfSpeech, difficultyLevel: Int) : this(mkId(lemma, language, partOfSpeech), difficultyLevel)

  companion object {
    private fun mkId(lemma: String, language: Language, partOfSpeech: PartOfSpeech): String {
      return "${language.code}:$lemma:$partOfSpeech"
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