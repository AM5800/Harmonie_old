package am5800.harmonie.app.model.services.learnGraph

import am5800.common.Language
import am5800.common.LearnGraphNode
import am5800.common.Sentence
import am5800.common.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.convert
import am5800.harmonie.app.model.services.KeyValueDatabase

class LearnGraphServiceImpl(private val loader: LearnGraphLoader,
                            private val db: KeyValueDatabase,
                            private val lifetime: Lifetime) : LearnGraphService {
  private val graphs = mutableMapOf<Language, List<LearnGraphNode>>()

  override fun unlockNextWordsGroup(learnLanguage: Language): List<Word> {
    val graph = ensureLoaded(learnLanguage)
    val property = getPositionProperty(learnLanguage)
    property.value = property.value!! + 1
    val rangeStart = property.value!!
    for (i in property.value!!..(graph.size - 1)) {
      if (graph[i].sentences.size == 0) continue
      property.value = i
      break
    }
    val rangeEnd = property.value!!
    return (rangeStart..rangeEnd).map { graph[it].word }
  }

  override fun canUnlockNextWordGroup(learnLanguage: Language): Boolean {
    val graph = ensureLoaded(learnLanguage)
    val property = getPositionProperty(learnLanguage)

    return property.value!! < graph.size - 1
  }

  override fun getUnlockedWords(learnLanguage: Language): List<Word> {
    val graph = ensureLoaded(learnLanguage)
    val property = getPositionProperty(learnLanguage)
    val rangeStart = 0
    val rangeEnd = property.value!!
    return (rangeStart..rangeEnd).map { graph[it].word }
  }

  override fun getUnlockedSentences(learnLanguage: Language): List<Sentence> {
    val graph = ensureLoaded(learnLanguage)
    val property = getPositionProperty(learnLanguage)
    val rangeStart = 0
    val rangeEnd = property.value!!
    return (rangeStart..rangeEnd).flatMap { graph[it].sentences }
  }

  private fun ensureLoaded(learnLanguage: Language): List<LearnGraphNode> {
    val graph = graphs[learnLanguage]
    if (graph != null) return graph

    val result = loader.load(learnLanguage)
    graphs[learnLanguage] = result

    val property = getPositionProperty(learnLanguage)
    val rangeStart = property.value ?: 0
    for (i in rangeStart..(result.size - 1)) {
      if (result[i].sentences.size == 0) continue
      property.value = i
      break
    }

    return result
  }

  private fun getSettingsKeyName(learnLanguage: Language): String {
    return "$settingsKey:${learnLanguage.code}"
  }

  private fun getPositionProperty(learnLanguage: Language): Property<Int> {
    // TODO: cache
    return db.createProperty(lifetime, getSettingsKeyName(learnLanguage), "0").convert({ it?.toInt() }, { it?.toString() })
  }

  companion object {
    val settingsKey = "LearnGraphPosition"
  }
}