package am5800.harmonie.app.vm

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.localization.LocalizationService

open class CheckableLanguageViewModel(lifetime: Lifetime,
                                      val language: Language,
                                      defaultChecked: Boolean,
                                      defaultVisible: Boolean) {
  val checked = Property(lifetime, defaultChecked)
  val title = language.nameInLanguage()
  val visible = Property(lifetime, defaultVisible)
}

class CheckableLanguageWithCounterViewModel(lifetime: Lifetime,
                                            language: Language,
                                            defaultChecked: Boolean,
                                            defaultVisible: Boolean,
                                            localizationService: LocalizationService) : CheckableLanguageViewModel(lifetime, language, defaultChecked, defaultVisible) {
  val count = Property(lifetime, 0)
  val countText = localizationService.createQuantityProperty({ it.nSentencesAvailable }, count, lifetime)
}