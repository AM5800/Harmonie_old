package am5800.harmonie.app.model.localization

import am5800.common.Language


class EnglishQuantityString(private val one: String?,
                            private val other: String?) : QuantityString {
  override fun build(value: Int): String {
    if (value == 1) return build(value, one)
    return build(value, other)
  }

  private fun build(value: Int, selectedString: String?): String {
    if (selectedString == null) throw Exception("Matching string not set")
    return selectedString.replace("%i", value.toString())
  }

}

class EnglishLocalizationTable : LocalizationTable {
  override val language: Language = Language.English

  override val continueButton: String = "Continue"

  override val minutesLeft: QuantityString = EnglishQuantityString("%i minute left", "%i minutes left")
  override val secondsLeft: QuantityString = EnglishQuantityString("%i second left", "%i seconds left")
}