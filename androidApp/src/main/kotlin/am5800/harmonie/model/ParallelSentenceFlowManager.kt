package am5800.harmonie.model

import am5800.common.Language
import am5800.common.utilityFunctions.shuffle
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.model.dbAccess.SentenceProvider

class ParallelSentencePresentation(val question: String, val answer: String)

class ParallelSentenceFlowManager(lifetime: Lifetime, private val sentenceProvider: SentenceProvider) : FlowItemProvider {
  val question = Property<ParallelSentencePresentation?>(lifetime, null)

  override fun tryPresentNextItem(flowSettings: FlowSettings): Boolean {
    val pair = sentenceProvider.getSentences(Language.German, Language.English).shuffle().first()
    question.value = ParallelSentencePresentation(pair.first.text, pair.second.text)
    return true
  }
}