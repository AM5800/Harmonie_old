package am5800.common

data class CommonLemma(override val id: String,
                       override val lemma: String,
                       override val language: Language,
                       override val partOfSpeech: PartOfSpeech,
                       override val difficultyLevel: Int) : Lemma {
  companion object {
    fun fromId(id: String, level: Int): Lemma {
      val chunks = id.split(':')
      if (chunks.size != 3) throw Exception("Unexpected id: " + id)
      val language = Language.parse(chunks[0])
      val lemma = chunks[1]
      val pos = PartOfSpeech.parse(chunks[2])

      return CommonLemma(id, lemma, language, pos, level)
    }
  }
}