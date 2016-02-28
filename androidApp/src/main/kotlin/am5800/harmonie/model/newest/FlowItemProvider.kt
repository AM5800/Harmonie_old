package am5800.harmonie.model.newest


interface FlowItemProvider {
  fun tryPresentNextItem(flowSettings: FlowSettings): Boolean
}