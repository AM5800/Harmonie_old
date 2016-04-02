package am5800.common

enum class Language(val code: String) {
  English("en") {
    override fun nameInLanguage(): String {
      return "English"
    }
  },
  German("de") {
    override fun nameInLanguage(): String {
      return "Deutsch"
    }
  },
  Russian("ru") {
    override fun nameInLanguage(): String {
      return "Русский"
    }
  },
  Japanese("jp") {
    override fun nameInLanguage(): String {
      return "日本語"
    }
  };

  abstract fun nameInLanguage(): String
}

class LanguageParser {
  companion object {
    fun parse(string: String): Language {
      return tryParse(string) ?: throw Exception("Unknown language code: $string")
    }

    fun tryParse(string: String?): Language? {
      if (string == null) return null
      val code = string.toLowerCase()
      return Language.values().firstOrNull() { it.code == code }
    }
  }
}



