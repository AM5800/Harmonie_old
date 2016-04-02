package am5800.harmonie.app.model.localization.languages

import am5800.harmonie.app.model.localization.QuantityStringBase

class RussianQuantityString(private val one: String?,
                            private val many: String?,
                            private val other: String?) : QuantityStringBase() {
  override fun build(value: Int): String {
    val mod10 = value % 10
    if (mod10 == 1) return build(value, one)
    if (mod10 < 5 && mod10 > 1) return build(value, many)
    return build(value, other)
  }
}