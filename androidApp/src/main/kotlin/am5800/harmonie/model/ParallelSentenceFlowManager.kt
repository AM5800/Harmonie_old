package am5800.harmonie.model

import utils.Lifetime
import utils.Signal

data class ParallelSentencePresentation(val question: String)

class ParallelSentenceFlowManager(lifetime: Lifetime) : FlowItemProvider {
  val presentationRequested = Signal<ParallelSentencePresentation>(lifetime)

  override fun tryPresentNextItem(flowSettings: FlowSettings): Boolean {
    presentationRequested.fire(ParallelSentencePresentation("Hello world"))
    return true
  }
}