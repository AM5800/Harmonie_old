package am5800.harmonie.app.vm.workspace

import am5800.common.utils.EnumerableDistribution
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.ConstantProperty
import am5800.harmonie.app.model.features.flow.FlowItemCategory
import am5800.harmonie.app.model.features.flow.FlowManager

class SimpleWorkspaceItemViewModel(val header: String,
                                   private val categories: Collection<FlowItemCategory>,
                                   private val lifetime: Lifetime,
                                   private val flowManager: FlowManager) {
  fun computeBriefStats(): SimpleWorkspaceItemBriefStats {
    return SimpleWorkspaceItemBriefStats(
        ConstantProperty(25, lifetime),
        ConstantProperty(50, lifetime),
        ConstantProperty(2000, lifetime))
  }

  fun onCommand() {
    flowManager.start(EnumerableDistribution.define {
      equal(categories)
    })
  }
}