package am5800.harmonie.app.model.services.flow

interface FlowItemProvider {
  fun tryPresentNextItem(tag: FlowItemTag): Boolean
}