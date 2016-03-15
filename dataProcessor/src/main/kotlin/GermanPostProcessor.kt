import am5800.common.Language

class GermanPostProcessor : SentencePostProcessor {
  override val language: Language = Language.German

  override fun processInPlace(occurrences: MutableList<ParseWordOccurrence>) {
    occurrences.removeAll { !it.lemma.all { c -> c.isLetter() } }
  }
}