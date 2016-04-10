package am5800.harmonie.app.vm

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.features.localization.LocalizationService
import am5800.harmonie.app.model.services.PreferredLanguagesService

class WelcomeScreenViewModel(private val lifetime: Lifetime,
                             localizationService: LocalizationService,
                             private val preferredLanguagesService: PreferredLanguagesService) : ViewModelBase(lifetime) {

  val knownLanguages = ObservableCollection<CheckableLanguageViewModel>(lifetime)
  val learnLanguages = ObservableCollection<CheckableLanguageViewModel>(lifetime)
  val welcome = localizationService.createProperty(lifetime, { it.welcomeToHarmonie })
  val chooseKnown = localizationService.createProperty(lifetime, { it.chooseKnownLanguage })
  val chooseLearn = localizationService.createProperty(lifetime, { it.chooseLearnLanguage })
  val continueBtnText = localizationService.createProperty(lifetime, { it.continueButton })

  val learnGroupVisible = Property(lifetime, false)
  val continueBtnVisible = Property(lifetime, false)

  fun next() {
    preferredLanguagesService.knownLanguages.value = knownLanguages.filter { it.checked.value!! }.map { it.language }
    preferredLanguagesService.learnLanguages.value = learnLanguages.filter { it.checked.value!! }.map { it.language }
    closeRequested.fire(Unit)
  }

  fun canCloseNow(): Boolean {
    return !preferredLanguagesService.configurationRequired
  }

  init {
    preferredLanguagesService.getAvailableKnownLanguages().forEach { lang -> setupCheckableVm(lang, knownLanguages) }

    val checkedKnownLanguages = knownLanguages.filterObservable { it.checked.value!! }

    checkedKnownLanguages.changed.subscribe(lifetime, {
      learnLanguages.clear()
      val translationLanguages = checkedKnownLanguages.map { it.language }.flatMap { preferredLanguagesService.getAvailableLearnLanguages(it) }.distinct()
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

  fun activateIfNeeded() {
    if (preferredLanguagesService.configurationRequired) {
      activationRequested.fire(Unit)
    }
  }
}