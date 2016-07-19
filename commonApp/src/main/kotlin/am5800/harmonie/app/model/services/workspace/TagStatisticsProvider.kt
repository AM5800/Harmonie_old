package am5800.harmonie.app.model.services.workspace

import am5800.harmonie.app.model.services.flow.FlowItemTag


interface TagStatisticsProvider {
  fun getTotalCount(tags: Collection<FlowItemTag>): Int
  fun getOnDueCount(tags: Collection<FlowItemTag>): Int
  fun getOnLearningCount(tags: Collection<FlowItemTag>): Int
}