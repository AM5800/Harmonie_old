package am5800.harmonie.app.model.localization.languages

import am5800.common.Language
import am5800.harmonie.app.model.localization.LocalizationTable
import am5800.harmonie.app.model.localization.QuantityString


class EnglishLocalizationTable : LocalizationTable {
  override val chooseLanguages = "Choose languages"
  override val learnAll = "Learn all"
  override val welcomeToHarmonie = "Welcome to Harmonie!"
  override val chooseKnownLanguage = "Choose languages you know well:"
  override val chooseLearnLanguage = "Choose languages you want to learn:"
  override val language: Language = Language.English

  override val continueButton: String = "Continue"

  override val minutesLeft: QuantityString = EnglishQuantityString("%i minute left", "%i minutes left")
  override val secondsLeft: QuantityString = EnglishQuantityString("%i second left", "%i seconds left")
}