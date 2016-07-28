package am5800.harmonie.android

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.localization.LocalizationService
import am5800.harmonie.app.model.localization.LocalizationServiceImpl
import am5800.harmonie.app.model.sql.KeyValueDatabase
import android.content.res.Resources
import java.util.*

class AndroidLocalizationService {
  companion object {
    fun create(resources: Resources, keyValueDatabase: KeyValueDatabase, lifetime: Lifetime): LocalizationService {
      val locale = resources.configuration.locale
      val language = getLanguage(locale, Language.English)
      return LocalizationServiceImpl(language, lifetime, keyValueDatabase)
    }

    private fun getLanguage(locale: Locale, defaultLanguage: Language): Language {
      // https://en.wikipedia.org/wiki/ISO_639-1
      return when (locale.language) {
        "ru" -> Language.Russian
        "en" -> Language.English
        else -> defaultLanguage
      }
    }
  }
}