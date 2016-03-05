package am5800.harmonie.app.model.flow


class FlowItemProviderRegistrar(parallelSentenceFlowManager: ParallelSentenceFlowManager) {
  val all: List<FlowItemProvider> = listOf(parallelSentenceFlowManager)
}