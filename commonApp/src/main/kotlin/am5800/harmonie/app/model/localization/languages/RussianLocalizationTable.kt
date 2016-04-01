package am5800.harmonie.app.model.localization.languages

import am5800.common.Language
import am5800.harmonie.app.model.localization.LocalizationTable

class RussianLocalizationTable : LocalizationTable {
  override val language: Language = Language.Russian
  override val continueButton: String = "Продолжить"
  override val minutesLeft = RussianQuantityString("Осталась %i минута", "Осталось %i минуты", "Осталось %i минут")
  override val secondsLeft = RussianQuantityString("Осталась %i секунда", "Осталось %i секунды", "Осталось %i секунд")
}

