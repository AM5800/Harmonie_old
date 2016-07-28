package am5800.harmonie.app.model.workspace

import am5800.harmonie.app.model.flow.FlowItemTag


interface TagStatisticsProvider {
  fun getTotalCount(tags: Collection<FlowItemTag>): Int
  fun getOnDueCount(tags: Collection<FlowItemTag>): Int
  fun getOnLearningCount(tags: Collection<FlowItemTag>): Int
}