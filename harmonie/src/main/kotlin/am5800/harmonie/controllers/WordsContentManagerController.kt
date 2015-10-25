package am5800.harmonie.controllers

import am5800.harmonie.controllers.ContentManagerControllerProvider
import am5800.harmonie.controllers.FlowItemController
import am5800.harmonie.controllers.GermanRecallController
import am5800.harmonie.model.*


public class WordsContentManagerController(private val renderer: ExamplesRenderer,
                                           private val entityManager: GermanEntityManager,
                                           private val markErrorHelper : MarkErrorHelper) : ContentManagerControllerProvider {
    override fun tryGetController(itemId: EntityId): FlowItemController? {
        val examples = entityManager.getExamples(itemId)
        if (examples.isEmpty()) return null

        val example = examples.shuffle().first()

        val renderedExample = renderer.render(example)
        return GermanRecallController(renderedExample, {markErrorHelper.markError(renderedExample)})
    }
}