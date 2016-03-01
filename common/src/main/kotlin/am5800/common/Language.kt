package am5800.common

enum class Language {
  English,
  German
}

class LanguageParser {
  companion object {
    fun parse(string: String): Language {
      return when (string.toLowerCase()) {
        "en" -> Language.English
        "de" -> Language.German
        else -> throw Exception("Unknown language: $string")
      }
    }

    fun toShort(language: Language): String {
      return when (language) {
        Language.English -> "en"
        Language.German -> "de"
      }
    }
  }
}



