package am5800.harmonie.app.vm

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.Property

class CheckableLanguageViewModel(lifetime: Lifetime, val language: Language, defaultChecked: Boolean) {
  val checked = Property(lifetime, defaultChecked)
  val title = language.nameInLanguage()
}