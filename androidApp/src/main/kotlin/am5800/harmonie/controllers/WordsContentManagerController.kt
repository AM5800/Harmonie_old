package am5800.harmonie.controllers

import am5800.harmonie.model.EntityId
import am5800.harmonie.model.ExamplesRenderer
import am5800.harmonie.model.GermanEntityManager
import am5800.harmonie.model.MarkErrorHelper
import utilityFunctions.shuffle


class WordsContentManagerController(private val renderer: ExamplesRenderer,
                                    private val entityManager: GermanEntityManager,
                                    private val markErrorHelper: MarkErrorHelper) : ContentManagerControllerProvider {
  override fun tryGetController(itemId: EntityId): FlowItemController? {
    val examples = entityManager.getExamples(itemId)
    if (examples.isEmpty()) return null

    val example = examples.shuffle().first()

    val renderedExample = renderer.render(example)
    return GermanRecallController(renderedExample, { markErrorHelper.markError(renderedExample) })
  }
}