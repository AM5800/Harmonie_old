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
  val uncertain: String
  val clear: String
  val sendDb: String
  val sendDbDescription: String
  val wordsList: String
  val wordsListDescription: String
  val noTranslation: String
  val daysLeft: QuantityString
  val hoursLeft: QuantityString
  val onDueStatus: String
  val lessThanMinute: String
}