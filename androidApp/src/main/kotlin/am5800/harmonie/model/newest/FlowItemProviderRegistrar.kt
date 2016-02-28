package am5800.harmonie.model.newest


class FlowItemProviderRegistrar(parallelSentenceFlowManager: ParallelSentenceFlowManager) {
  val all: List<FlowItemProvider> = listOf(parallelSentenceFlowManager)
}