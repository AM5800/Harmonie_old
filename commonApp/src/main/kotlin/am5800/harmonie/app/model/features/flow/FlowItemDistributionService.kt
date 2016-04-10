package am5800.harmonie.app.model.features.flow

import am5800.harmonie.app.model.services.PreferredLanguagesService

class FlowItemDistributionService(private val providers: Collection<FlowItemProvider>, private val preferredLanguagesService: PreferredLanguagesService) {
  fun getDistribution(): CategoryDistribution {
    val allCategories = providers.flatMap { it.supportedCategories }.distinct().filter {
      if (it !is LanguageCategory) return@filter true
      if (!preferredLanguagesService.knownLanguages.value!!.contains(it.knownLanguage)) return@filter false
      if (!preferredLanguagesService.learnLanguages.value!!.contains(it.learnLanguage)) return@filter false
      return@filter true
    }
    return CategoryDistribution(allCategories.map { Pair(it, 1.0 / allCategories.size) }.toMap())
  }
}