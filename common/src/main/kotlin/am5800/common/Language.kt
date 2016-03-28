package am5800.common

enum class Language(val code: String) {
  English("en"),
  German("de"),
  Russian("ru"),
  Japanese("jp")
}

class LanguageParser {
  companion object {
    fun parse(string: String): Language {
      val code = string.toLowerCase()
      return Language.values().firstOrNull() { it.code == code }
          ?: throw Exception("Unknown language code: $string")
    }
  }
}



