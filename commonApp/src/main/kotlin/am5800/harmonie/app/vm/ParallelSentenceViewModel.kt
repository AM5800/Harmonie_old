package am5800.harmonie.app.vm

import am5800.common.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.ReadonlyProperty
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.localization.LocalizationService
import am5800.harmonie.app.model.repetition.LearnScore
import java.util.*

open class WordViewModel(val text: String, val needSpaceBefore: Boolean)

class ToggleableWordViewModel(val word: Word, text: String,
                              val state: Property<LearnScore>,
                              needSpaceBefore: Boolean,
                              val highlight: Boolean) : WordViewModel(text, needSpaceBefore) {
  fun toggle() {
    if (state.value == LearnScore.Good) state.value = LearnScore.Bad
    else state.value = LearnScore.Good
  }
}

class ParallelSentenceViewModel(lifetime: Lifetime,
                                private val parallelSentenceFlowManager: ParallelSentenceFlowManager,
                                private val flowManager: FlowManager,
                                localizationService: LocalizationService) : ViewModel by ViewModelBase(lifetime) {
  enum class State {
    ShowQuestion,
    ShowAnswer
  }

  val continueBtnText = localizationService.createProperty(lifetime, { it.continueButton })

  private val _state = Property(lifetime, State.ShowQuestion)
  val state: ReadonlyProperty<State>
    get() = _state


  fun next() {
    if (_state.value == State.ShowQuestion) {
      _state.value = State.ShowAnswer
    } else {
      val scores = LinkedHashMap<Word, LearnScore>()
      val vms = question.value?.filterIsInstance<ToggleableWordViewModel>() ?: emptyList()
      for (vm in vms) {
        scores.put(vm.word, vm.state.value!!)
      }
      parallelSentenceFlowManager.submitScore(scores)
      flowManager.next()
    }
  }

  val answerGroupVisibility = Property(lifetime, false)
  val question = Property(lifetime, emptyList<WordViewModel>())
  val answer = Property(lifetime, "")

  init {
    _state.forEachValue(lifetime, { state, lt ->
      if (state == State.ShowQuestion) {
        answerGroupVisibility.value = false
      } else if (state == State.ShowAnswer) {
        answerGroupVisibility.value = true
      }
    })

    parallelSentenceFlowManager.question.forEachValue(lifetime, { data, lt ->
      data!!
      _state.value = State.ShowQuestion
      question.value = createViewModelsForQuestion(data, lifetime)
      answer.value = data.answer.text
      activationRequired.fire(Unit)
    })
  }
}