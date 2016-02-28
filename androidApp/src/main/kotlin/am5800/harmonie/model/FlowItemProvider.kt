package am5800.harmonie.model


interface FlowItemProvider {
  fun tryPresentNextItem(flowSettings: FlowSettings): Boolean
}