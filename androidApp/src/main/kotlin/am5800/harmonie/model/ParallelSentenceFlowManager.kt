package am5800.harmonie.model

import utils.Lifetime
import utils.Property

data class ParallelSentencePresentation(val question: String, val answer: String)

class ParallelSentenceFlowManager(lifetime: Lifetime) : FlowItemProvider {
  val question = Property<ParallelSentencePresentation?>(lifetime, null)

  override fun tryPresentNextItem(flowSettings: FlowSettings): Boolean {
    question.value = ParallelSentencePresentation("Hello world", "Привет мир")
    return true
  }
}