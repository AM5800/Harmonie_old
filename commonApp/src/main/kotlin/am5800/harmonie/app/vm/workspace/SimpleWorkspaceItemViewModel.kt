package am5800.harmonie.app.vm.workspace

import am5800.common.utils.EnumerableDistribution
import am5800.harmonie.app.model.services.flow.FlowItemTag
import am5800.harmonie.app.model.services.flow.FlowManager
import am5800.harmonie.app.model.services.workspace.TagStatisticsProvider

class SimpleWorkspaceItemViewModel(val header: String,
                                   private val tags: Collection<FlowItemTag>,
                                   private val tagStatisticsProvider: TagStatisticsProvider,
                                   private val flowManager: FlowManager) {
  fun computeBriefStats(): SimpleWorkspaceItemBriefStats {


    val onDue = tagStatisticsProvider.getOnDueCount(tags)
    val onLearning = tagStatisticsProvider.getOnLearningCount(tags)
    val total = tagStatisticsProvider.getTotalCount(tags)
    return SimpleWorkspaceItemBriefStats(onDue, onLearning, total)
  }

  fun onCommand() {
    flowManager.start(EnumerableDistribution.define {
      equal(tags)
    })
  }
}