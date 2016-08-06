package am5800.harmonie.app.model.localization.languages

import am5800.common.Language
import am5800.harmonie.app.model.localization.FormatStringImpl
import am5800.harmonie.app.model.localization.LocalizationTable

class RussianLocalizationTable : LocalizationTable {
  override val lessThanMinute = "меньше минуты"
  override val daysLeft = EnglishQuantityString("%i day left", "%i days left")
  override val hoursLeft = EnglishQuantityString("%i hour left", "%i hours left")
  override val onDueStatus = "On due"
  override val noTranslation = "Перевод недоступен"
  override val sendDb = "Send DB"
  override val sendDbDescription = "Send internal database to the author"
  override val wordsList = "Words list"
  override val wordsListDescription = "View and edit words status"
  override val unclear = "Не понятно"
  override val blackout = "По нулям"
  override val uncertain = "Не уверен"
  override val clear = "Понятно"
  override val onDue = FormatStringImpl("Повторить: %0")
  override val onLearning = FormatStringImpl("На изучении: %0/%1")
  override val nSentencesAvailable = RussianQuantityString("%i предложение", "%i предложения", "%i предложений")
  override val parallelSentencesQuizHelp = "Читайте предложения и сверяйтесь с переводом. Отмечайте слова, в которых вы ошиблись или которые не знали"
  override val reportUnclearSentencePair = "Сообщить: неудачный пример"
  override val reportWrongTranslation = "Сообщить: неправильный перевод"
  override val reportOther = "Сообщить: другое"
  override val sendStatistics = "Отправить статистику"
  override val lessonIsOver = "Урок окончен"
  override val chooseLanguages = "Выбрать языки"
  override val learnAll = "Учить все"
  override val language: Language = Language.Russian
  override val continueButton = "Продолжить"
  override val showTranslation = "Показать перевод"
  override val minutesLeft = RussianQuantityString("Осталась %i минута", "Осталось %i минуты", "Осталось %i минут")
  override val secondsLeft = RussianQuantityString("Осталась %i секунда", "Осталось %i секунды", "Осталось %i секунд")
  override val welcomeToHarmonie = "Добро пожаловать в Harmonie!"
  override val chooseKnownLanguage = "Выберите языки, которые вы знаете хорошо:"
  override val chooseLearnLanguage = "Выберите языки, которые хотели бы изучить:"
}

