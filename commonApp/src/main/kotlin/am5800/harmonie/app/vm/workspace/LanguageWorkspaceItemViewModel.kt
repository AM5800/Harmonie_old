package am5800.harmonie.app.vm.workspace

import am5800.common.utils.EnumerableDistribution
import am5800.harmonie.app.model.flow.FlowItemTag
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.workspace.TagStatisticsProvider

class LanguageWorkspaceItemViewModel(val header: String,
                                     private val tags: Collection<FlowItemTag>,
                                     private val tagStatisticsProvider: TagStatisticsProvider,
                                     private val flowManager: FlowManager) : WorkspaceItemViewModel {
  override val action: () -> Unit
    get() = {
      flowManager.start(EnumerableDistribution.define {
        equal(tags)
      })
    }

  fun computeBriefStats(): LanguageWorkspaceItemBriefStats {
    val onDue = tagStatisticsProvider.getOnDueCount(tags)
    val onLearning = tagStatisticsProvider.getOnLearningCount(tags)
    val total = tagStatisticsProvider.getTotalCount(tags)
    return LanguageWorkspaceItemBriefStats(onDue, onLearning, total)
  }
}