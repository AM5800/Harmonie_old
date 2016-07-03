package am5800.harmonie.app.model.services.learnGraph

import am5800.common.Language
import am5800.common.LearnGraphNode


interface LearnGraphLoader {
  fun load(learnLanguage: Language): List<LearnGraphNode>
}