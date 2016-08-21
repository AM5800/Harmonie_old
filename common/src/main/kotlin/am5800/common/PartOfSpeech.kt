package am5800.common

enum class PartOfSpeech {
  Verb,
  Noun,
  ProperNoun,
  Article,
  Preposition,
  Other,
  Punctuation,
  ForeignLanguage,
  Conjunction,
  Pronoun,
  Adverb,
  Adjective,
  Digit;

  companion object {
    fun parse(string: String): PartOfSpeech {
      return PartOfSpeech.valueOf(string)
    }
  }
}