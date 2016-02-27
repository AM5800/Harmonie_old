data class SentenceGroup(val sentences: Map<Language, String>) {
  class SentenceDefinition {
    fun sentence(language: Language, text: String) {
      sentences.put(language, text)
    }

    val sentences = mutableMapOf<Language, String>()
  }

  fun byLang(language: Language): String? {
    return sentences[language]
  }

  companion object {
    fun create(init: SentenceDefinition. () -> Unit): SentenceGroup {
      val def = SentenceDefinition()
      init(def)
      return SentenceGroup(def.sentences)
    }
  }
}