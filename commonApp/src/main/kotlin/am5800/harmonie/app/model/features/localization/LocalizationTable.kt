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
  val reportUnclearSentencePair: String
  val reportWrongTranslation: String
  val reportOther: String
  val parallelSentencesQuizHelp: String
  val nSentencesAvailable: QuantityString
  val onDue: FormatString
  val onLearning: FormatString
  val showTranslation: String
  val unclear: String
  val blackout: String
  val uncertain: String
  val clear: String
}