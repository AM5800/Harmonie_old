package am5800.harmonie.app.model.features.localization

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.ReadonlyProperty
import am5800.common.utils.properties.convert
import am5800.harmonie.app.model.features.localization.languages.EnglishLocalizationTable
import am5800.harmonie.app.model.features.localization.languages.RussianLocalizationTable
import am5800.harmonie.app.model.services.KeyValueDatabase

interface LocalizationService {
  fun createProperty(lifetime: Lifetime, valueGetter: (LocalizationTable) -> String): ReadonlyProperty<String>
  fun createQuantityProperty(valueGetter: (LocalizationTable) -> QuantityString, quantity: ReadonlyProperty<Int>, lifetime: Lifetime): ReadonlyProperty<String>
  fun setLanguage(language: Language)
  fun getCurrentTable(): LocalizationTable
}

open class LocalizationServiceImpl(private val defaultLanguage: Language,
                                   lifetime: Lifetime,
                                   keyValueDatabase: KeyValueDatabase) : LocalizationService {

  private val currentLanguage = keyValueDatabase.createProperty(lifetime, "LocalizationService.currentLanguage", defaultLanguage.toString())
      .convert({ LanguageParser.tryParse(it) ?: defaultLanguage }, { it.toString() })

  private val tables = listOf(EnglishLocalizationTable(), RussianLocalizationTable())

  override fun createProperty(lifetime: Lifetime, valueGetter: (LocalizationTable) -> String): ReadonlyProperty<String> {
    val result = Property(lifetime, "")
    currentLanguage.onChange(lifetime, { result.value = valueGetter(getCurrentTable()) })
    return result
  }

  override fun getCurrentTable(): LocalizationTable {
    val language = currentLanguage.value
    return tables.single { it.language == language }
  }

  override fun createQuantityProperty(valueGetter: (LocalizationTable) -> QuantityString, quantity: ReadonlyProperty<Int>, lifetime: Lifetime): ReadonlyProperty<String> {
    val result = Property(lifetime, "")
    val updateValue = { result.value = valueGetter(getCurrentTable()).build(quantity.value) }
    currentLanguage.onChange(lifetime, { updateValue() })
    quantity.onChange(lifetime, { updateValue() })
    return result
  }

  override fun setLanguage(language: Language) {
    currentLanguage.value = language
  }
}