package am5800.harmonie.app.model.localization.languages

import am5800.common.Language
import am5800.harmonie.app.model.localization.FormatStringImpl
import am5800.harmonie.app.model.localization.LocalizationTable


class EnglishLocalizationTable : LocalizationTable {
  override val daysLeft = EnglishQuantityString("%i day left", "%i days left")
  override val hoursLeft = EnglishQuantityString("%i hour left", "%i hours left")
  override val onDueStatus = "On due"
  override val noTranslation = "Translation not available"
  override val sendDb = "Send DB"
  override val sendDbDescription = "Send internal database to the author"
  override val wordsList = "Words list"
  override val wordsListDescription = "View and edit words status"
  override val unclear = "Unclear"
  override val blackout = "Blackout"
  override val uncertain = "Uncertain"
  override val clear = "Clear"
  override val onDue = FormatStringImpl("On due: %0")
  override val onLearning = FormatStringImpl("On learning: %0/%1")
  override val nSentencesAvailable = EnglishQuantityString("%i sentence", "%i sentences")
  override val parallelSentencesQuizHelp = "Read sentences and check that you understand them right. Mark words you translated wrong/do not know"
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
  override val showTranslation: String = "Show translation"
  override val minutesLeft = EnglishQuantityString("%i minute left", "%i minutes left")
  override val secondsLeft = EnglishQuantityString("%i second left", "%i seconds left")
}