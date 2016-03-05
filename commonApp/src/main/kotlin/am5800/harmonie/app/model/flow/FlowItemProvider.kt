package am5800.harmonie.app.model.flow


interface FlowItemProvider {
  fun tryPresentNextItem(flowSettings: FlowSettings): Boolean
}