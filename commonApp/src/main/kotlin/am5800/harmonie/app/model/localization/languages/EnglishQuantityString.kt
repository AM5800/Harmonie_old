package am5800.harmonie.app.model.localization.languages

import am5800.harmonie.app.model.localization.QuantityStringBase

class EnglishQuantityString(private val one: String?,
                            private val other: String?) : QuantityStringBase() {
  override fun build(value: Int): String {
    if (value == 1) return build(value, one)
    return build(value, other)
  }
}