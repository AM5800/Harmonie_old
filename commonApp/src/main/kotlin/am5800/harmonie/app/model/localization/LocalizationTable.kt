package am5800.harmonie.app.model.localization

import am5800.common.Language


interface LocalizationTable {
  val language: Language
  val continueButton: String
  val minutesLeft: QuantityString
  val secondsLeft: QuantityString
  val welcomeToHarmonie: String
  val chooseKnownLanguage: String
  val chooseLearnLanguage: String
}