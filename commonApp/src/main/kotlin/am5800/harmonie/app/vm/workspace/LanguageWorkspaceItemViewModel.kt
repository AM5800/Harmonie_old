package am5800.harmonie.app.vm.workspace

import am5800.common.utils.EnumerableDistribution
import am5800.harmonie.app.model.flow.FlowItemTag
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.workspace.TagStatisticsProvider

class LanguageWorkspaceItemViewModel(val title: String,
                                     private val tags: EnumerableDistribution<FlowItemTag>,
                                     private val tagStatisticsProvider: TagStatisticsProvider,
                                     private val flowManager: FlowManager) : WorkspaceItemViewModel {
  override val action: () -> Unit
    get() = {
      flowManager.start(tags)
    }

  fun computeBriefStats(): LanguageWorkspaceItemBriefStats {
    val onDue = tagStatisticsProvider.getOnDueCount(tags.items)
    val onLearning = tagStatisticsProvider.getOnLearningCount(tags.items)
    val total = tagStatisticsProvider.getTotalCount(tags.items)
    return LanguageWorkspaceItemBriefStats(onDue, onLearning, total)
  }
}