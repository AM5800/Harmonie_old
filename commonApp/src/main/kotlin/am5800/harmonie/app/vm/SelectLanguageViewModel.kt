package am5800.harmonie.app.vm

import am5800.common.EntityCounter
import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.features.localization.LocalizationService
import am5800.harmonie.app.model.services.PreferredLanguagesService

class SelectLanguageViewModel(private val lifetime: Lifetime,
                              localizationService: LocalizationService,
                              private val preferredLanguagesService: PreferredLanguagesService) : ViewModelBase(lifetime) {

  val knownLanguages = preferredLanguagesService.getAvailableKnownLanguages().map {
    CheckableLanguageViewModel(lifetime, it, false, true)
  }
  val learnLanguages = Language.values().map {
    CheckableLanguageWithCounterViewModel(lifetime, it, false, false, localizationService)
  }
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
    for (vm in knownLanguages.plus(learnLanguages)) {
      vm.checked.onChange(lifetime, { if (!it.isAcknowledge) update() })
    }
    activationRequested.subscribe(lifetime, {
      val selectedKnownLanguages = preferredLanguagesService.knownLanguages.value!!.toSet()
      for (knownLanguageVm in knownLanguages) {
        knownLanguageVm.checked.value = selectedKnownLanguages.contains(knownLanguageVm.language)
      }

      val selectedLearnLanguages = preferredLanguagesService.learnLanguages.value!!.toSet()
      for (learnLanguageVm in learnLanguages) {
        learnLanguageVm.checked.value = selectedLearnLanguages.contains(learnLanguageVm.language)
      }

      update()
    })
  }

  private fun update() {
    val knownChecked = knownLanguages.filter { it.checked.value == true }

    if (knownChecked.isEmpty()) {
      learnGroupVisible.value = false
      continueBtnVisible.value = false
      return
    }

    updateLearnLanguages(knownChecked)

    val learnCheckedAndVisible = learnLanguages.filter { it.checked.value == true && it.visible.value == true }

    learnGroupVisible.value = true
    if (learnCheckedAndVisible.isEmpty()) {
      continueBtnVisible.value = false
      return
    }

    continueBtnVisible.value = true
  }

  private fun updateLearnLanguages(knownChecked: Collection<CheckableLanguageViewModel>) {
    val learnLanguagesCounter = EntityCounter<Language>()
    for (knownLanguage in knownChecked.map { it.language }) {
      val countables = preferredLanguagesService.getAvailableLearnLanguages(knownLanguage)
      learnLanguagesCounter.add(countables)
    }

    for (learnLanguageVm in learnLanguages) {
      val count = learnLanguagesCounter.result[learnLanguageVm.language]
      if (count == null) {
        learnLanguageVm.visible.value = false
      } else {
        learnLanguageVm.visible.value = true
        learnLanguageVm.count.value = count
      }
    }
  }

  fun activateIfNeeded() {
    if (preferredLanguagesService.configurationRequired) {
      activationRequested.fire(Unit)
    }
  }
}