package am5800.harmonie.app.model.features.localization.languages

import am5800.common.Language
import am5800.harmonie.app.model.features.localization.LocalizationTable
import am5800.harmonie.app.model.features.localization.QuantityString


class EnglishLocalizationTable : LocalizationTable {
  override val reportUnclearSentencePair = "Report: unclear meaning"
  override val reportWrongTranslation = "Report: translation error"
  override val reportOther = "Report: other"
  override val sendStatistics = "Send usage statistics"
  override val lessonIsOver = "Lesson is over"
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