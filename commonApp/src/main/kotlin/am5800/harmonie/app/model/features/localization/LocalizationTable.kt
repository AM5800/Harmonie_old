package am5800.harmonie.app.model.features.localization

import am5800.common.Language


interface LocalizationTable {
  val language: Language
  val continueButton: String
  val minutesLeft: QuantityString
  val secondsLeft: QuantityString
  val welcomeToHarmonie: String
  val chooseKnownLanguage: String
  val chooseLearnLanguage: String
  val learnAll: String
  val chooseLanguages: String
  val lessonIsOver: String
  val sendStatistics: String
}