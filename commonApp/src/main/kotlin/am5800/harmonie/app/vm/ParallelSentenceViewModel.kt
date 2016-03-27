package am5800.harmonie.app.vm

import am5800.common.db.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.ParallelSentenceFlowManager
import am5800.harmonie.app.model.repetition.AttemptScore
import java.util.*

open class WordViewModel(val text: String, val needSpaceBefore: Boolean)

class ToggleableWordViewModel(val word: Word, text: String,
                              val state: Property<AttemptScore>,
                              needSpaceBefore: Boolean,
                              val highlight: Boolean) : WordViewModel(text, needSpaceBefore) {
  fun toggle() {
    if (state.value == AttemptScore.Ok) state.value = AttemptScore.Wrong
    else state.value = AttemptScore.Ok
  }
}

class ParallelSentenceViewModel(lifetime: Lifetime,
                                private val parallelSentenceFlowManager: ParallelSentenceFlowManager,
                                private val flowManager: FlowManager) : ViewModel by ViewModelBase(lifetime) {
  private enum class State {
    ShowQuestion,
    ShowAnswer
  }

  private val state = Property(lifetime, State.ShowQuestion)


  fun next() {
    if (state.value == State.ShowQuestion) {
      state.value = State.ShowAnswer
    } else {
      val scores = LinkedHashMap<Word, AttemptScore>()
      val vms = question.value?.filterIsInstance<ToggleableWordViewModel>() ?: emptyList()
      for (vm in vms) {
        scores.put(vm.word, vm.state.value!!)
      }
      parallelSentenceFlowManager.submitScore(scores)
      flowManager.next()
    }
  }

  val answerGroupVisibility = Property(lifetime, Visibility.Collapsed)
  val question = Property(lifetime, emptyList<WordViewModel>())
  val answer = Property(lifetime, "")

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
      question.value = createViewModelsForQuestion(data, lifetime)
      answer.value = data.answer.text
      activationRequired.fire(Unit)
    })
  }
}