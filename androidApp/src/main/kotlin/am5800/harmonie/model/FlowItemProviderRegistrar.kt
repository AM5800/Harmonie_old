package am5800.harmonie.model


class FlowItemProviderRegistrar(parallelSentenceFlowManager: ParallelSentenceFlowManager) {
  val all: List<FlowItemProvider> = listOf(parallelSentenceFlowManager)
}