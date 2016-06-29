package testUtils

import am5800.common.Language
import am5800.common.LearnGraphNode
import am5800.harmonie.app.model.services.learnGraph.LearnGraphLoader

class LearnGraphLoaderMock(private val graph: List<LearnGraphNode>) : LearnGraphLoader {
  override fun load(learnLanguage: Language): List<LearnGraphNode> {
    return graph
  }
}