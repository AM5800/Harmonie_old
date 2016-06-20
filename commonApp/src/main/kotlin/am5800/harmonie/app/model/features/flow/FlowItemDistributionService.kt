package am5800.harmonie.app.model.features.flow

import am5800.harmonie.app.model.features.fillTheGap.FillTheGapCategory
import am5800.harmonie.app.model.services.EnumerableDistribution
import am5800.harmonie.app.model.services.PreferredLanguagesService
import com.google.common.collect.LinkedHashMultimap

fun createDefaultCategoryDistribution(categories: Collection<FlowItemCategory>): Map<FlowItemCategory, Double> {
  val map = LinkedHashMultimap.create<String, FlowItemCategory>()

  for (category in categories) {
    if (category is FillTheGapCategory) map.put(null, category)
    else map.put(category.toString(), category)
  }

  val baseF = 1.0 / map.keySet().size
  return map.asMap().flatMap { kvp ->
    kvp.value.map { Pair(it, baseF / kvp.value.size) }
  }.toMap()
}

class FlowItemDistributionService(private val providers: Collection<FlowItemProvider>, private val preferredLanguagesService: PreferredLanguagesService) {
  fun getDistribution(): EnumerableDistribution<FlowItemCategory> {
    val allCategories = providers.flatMap { it.supportedCategories }.distinct().filter {
      if (it !is LanguageCategory) return@filter true
      if (!preferredLanguagesService.selectedKnownLanguages.value!!.contains(it.knownLanguage)) return@filter false
      if (!preferredLanguagesService.selectedLearnLanguages.value!!.contains(it.learnLanguage)) return@filter false
      return@filter true
    }

    return EnumerableDistribution(createDefaultCategoryDistribution(allCategories))
  }
}