package am5800.common


interface Lemma {
  val id: String
  val lemma: String
  val language: Language
  val partOfSpeech: PartOfSpeech
  val difficultyLevel: Int
}