package am5800.harmonie.app.model.exercises.vplusp

import am5800.common.utils.Lifetime
import am5800.common.utils.functions.random
import am5800.common.utils.functions.randomOrNull
import am5800.common.utils.properties.NullableProperty
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.flow.FlowItemProvider
import am5800.harmonie.app.model.flow.FlowItemTag
import am5800.harmonie.app.model.repetition.LearnScore
import am5800.harmonie.app.model.repetition.RepetitionService
import am5800.harmonie.app.model.workspace.TagStatisticsProvider
import org.joda.time.DateTime

class VPlusPFlowManager(lifetime: Lifetime,
                        private val repetitionService: RepetitionService,
                        private val vPlusPDataProvider: VPlusPDataProvider,
                        private val debugOptions: DebugOptions) : FlowItemProvider, TagStatisticsProvider {
  private val category = "vplusp"

  override fun getTotalCount(tags: Collection<FlowItemTag>): Int {
    return vPlusPDataProvider.getAllTopics().size
  }

  override fun getOnDueCount(tags: Collection<FlowItemTag>): Int {
    return repetitionService.countOnDueItems(category, DateTime.now())
  }

  override fun getOnLearningCount(tags: Collection<FlowItemTag>): Int {
    return repetitionService.getAttemptedItems(category).count()
  }

  val currentItem = NullableProperty<VPlusPData>(lifetime, null)

  override fun tryPresentNextItem(tag: FlowItemTag): Boolean {
    if (tag !is VPlusPFlowItemTag) return false

    val nextScheduledItem = getNextScheduledItem(DateTime.now())
    if (nextScheduledItem != null) {
      currentItem.value = nextScheduledItem
      return true
    }

    val topic = vPlusPDataProvider.getAllTopics().randomOrNull(debugOptions.random) ?: return false
    currentItem.value = vPlusPDataProvider.get(topic).random(debugOptions.random)
    return true
  }

  fun submitResult(solvedOnFirstAttempt: Boolean) {
    val topic = currentItem.value!!.topic
    val score = if (solvedOnFirstAttempt) LearnScore.Good else LearnScore.Bad
    repetitionService.submitAttempt(topic, category, score)
  }

  fun getNextScheduledItem(dateTime: DateTime): VPlusPData? {
    while (true) {
      val topic = repetitionService.getNextScheduledEntity(category, dateTime) ?: return null
      val result = vPlusPDataProvider.get(topic).randomOrNull(debugOptions.random)
      if (result != null) return result
      else repetitionService.remove(topic, category)
    }
  }

  fun getKnownPrepositions(): List<String> {
    return vPlusPDataProvider.getKnownPrepositions()
  }
}