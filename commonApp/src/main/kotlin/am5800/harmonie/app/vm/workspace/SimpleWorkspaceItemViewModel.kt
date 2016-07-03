package am5800.harmonie.app.vm.workspace

import am5800.common.utils.EnumerableDistribution
import am5800.harmonie.app.model.features.flow.FlowItemCategory
import am5800.harmonie.app.model.features.flow.FlowManager

class SimpleWorkspaceItemViewModel(val header: String,
                                   private val categories: Collection<FlowItemCategory>,
                                   private val flowManager: FlowManager) {
  fun computeBriefStats(): SimpleWorkspaceItemBriefStats {
    return SimpleWorkspaceItemBriefStats(25, 50, 2000)
  }

  fun onCommand() {
    flowManager.start(EnumerableDistribution.define {
      equal(categories)
    })
  }
}