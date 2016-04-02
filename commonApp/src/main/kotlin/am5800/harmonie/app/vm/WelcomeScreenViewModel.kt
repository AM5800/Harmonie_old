package am5800.harmonie.app.vm

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.localization.LocalizationService
import am5800.harmonie.app.model.logging.LoggerProvider

class LanguageAvailability {
  fun getAvailableLanguages(): List<Language> {
    return listOf(Language.Russian, Language.English)
  }

  fun getAvailableTranslations(language: Language): List<Language> {
    return when (language) {
      Language.Russian -> listOf(Language.German, Language.English, Language.Japanese)
      Language.English -> listOf(Language.German, Language.Russian)
      else -> emptyList()
    }
  }
}

class WelcomeScreenViewModel(private val lifetime: Lifetime,
                             localizationService: LocalizationService,
                             private val languageAvailability: LanguageAvailability,
                             loggerProvider: LoggerProvider) : ViewModelBase(lifetime) {

  private val logger = loggerProvider.getLogger(this.javaClass)
  val knownLanguages = ObservableCollection<CheckableLanguageViewModel>(lifetime)
  val learnLanguages = ObservableCollection<CheckableLanguageViewModel>(lifetime)
  val welcome = localizationService.createProperty(lifetime, { it.welcomeToHarmonie })
  val chooseKnown = localizationService.createProperty(lifetime, { it.chooseKnownLanguage })
  val chooseLearn = localizationService.createProperty(lifetime, { it.chooseLearnLanguage })
  val continueBtnText = localizationService.createProperty(lifetime, { it.continueButton })

  val learnGroupVisible = Property(lifetime, false)
  val continueBtnVisible = Property(lifetime, false)


  init {
    languageAvailability.getAvailableLanguages().forEach { lang -> setupCheckableVm(lang, knownLanguages) }

    val checkedKnownLanguages = knownLanguages.filterObservable { it.checked.value!! }

    checkedKnownLanguages.changed.subscribe(lifetime, {
      learnLanguages.clear()
      val translationLanguages = checkedKnownLanguages.map { it.language }.flatMap { languageAvailability.getAvailableTranslations(it) }.distinct()
      translationLanguages.forEach { lang -> setupCheckableVm(lang, learnLanguages) }
    })

    val anyKnownLanguageChecked = checkedKnownLanguages.toProperty { collection -> collection.filterNotNull().any { it.checked.value!! } }
    anyKnownLanguageChecked.onChange(lifetime, learnGroupVisible)

    val learnLanguagesChecked = learnLanguages
        .filterObservable { it.checked.value!! }
        .toProperty { collection -> collection.filterNotNull().any { it.checked.value!! } }

    learnLanguagesChecked.onChange(lifetime, continueBtnVisible)
  }

  private fun setupCheckableVm(lang: Language, collection: ObservableCollection<CheckableLanguageViewModel>) {
    collection.add { lt ->
      val vm = CheckableLanguageViewModel(lt, lang, false)
      vm.checked.onChange(lt, { collection.changed.fire(Unit) })
      vm
    }
  }
}