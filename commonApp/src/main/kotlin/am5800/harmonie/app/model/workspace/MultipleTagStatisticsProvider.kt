package am5800.harmonie.app.model.workspace

import am5800.harmonie.app.model.flow.FlowItemTag

class MultipleTagStatisticsProvider(private val providers: List<TagStatisticsProvider>) : TagStatisticsProvider {
  override fun getTotalCount(tags: Collection<FlowItemTag>): Int {
    return providers.sumBy { it.getTotalCount(tags) }
  }

  override fun getOnDueCount(tags: Collection<FlowItemTag>): Int {
    return providers.sumBy { it.getOnDueCount(tags) }
  }

  override fun getOnLearningCount(tags: Collection<FlowItemTag>): Int {
    return providers.sumBy { it.getOnLearningCount(tags) }
  }
}