package am5800.common

enum class Language {
  English,
  German,
  Russian,
  Japanese
}

fun Language.code(): String {
  return when (this) {
    Language.English -> "en"
    Language.German -> "de"
    Language.Russian -> "ru"
    Language.Japanese -> "jp"
  }
}

class LanguageParser {
  companion object {
    fun parse(string: String): Language {
      return when (string.toLowerCase()) {
        "en" -> Language.English
        "de" -> Language.German
        "jp" -> Language.Japanese
        "ru" -> Language.Russian
        else -> throw Exception("Unknown language: $string")
      }
    }
  }
}



