package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.ParallelSentenceFlowManager
import am5800.harmonie.app.model.flow.ParallelSentenceUserScore

class ParallelSentenceViewModel(lifetime: Lifetime,
                                private val parallelSentenceFlowManager: ParallelSentenceFlowManager,
                                private val flowManager: FlowManager) : ViewModel by ViewModelBase(lifetime) {
  private enum class State {
    ShowQuestion,
    ShowAnswer
  }

  private val state = Property(lifetime, State.ShowQuestion)


  fun showAnswer() {
    state.value = State.ShowAnswer
  }

  fun submitAnswer(answer: ParallelSentenceUserScore) {
    parallelSentenceFlowManager.submitScore(answer)
    flowManager.next()
  }

  val answerGroupVisibility = Property<Visibility>(lifetime, Visibility.Collapsed)
  val question = Property<String>(lifetime, "")
  val answer = Property<String>(lifetime, "")

  init {
    state.forEachValue(lifetime, { state, lt ->
      if (state == State.ShowQuestion) {
        answerGroupVisibility.value = Visibility.Collapsed
      } else if (state == State.ShowAnswer) {
        answerGroupVisibility.value = Visibility.Visible
      }
    })

    parallelSentenceFlowManager.question.forEachValue(lifetime, { data, lt ->
      data!!
      state.value = State.ShowQuestion
      question.value = data.first.text
      answer.value = data.second.text
      activationRequired.fire(Unit)
    })
  }
}