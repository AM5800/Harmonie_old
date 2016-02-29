package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.controllers.defaultControls.ButtonController
import am5800.harmonie.controllers.defaultControls.TextViewController
import am5800.harmonie.model.FlowManager
import am5800.harmonie.model.ParallelSentenceFlowManager
import am5800.harmonie.viewBinding.ReflectionBindableController
import utils.Lifetime
import utils.Property

class ParallelSentenceController(parallelSentenceFlowManager: ParallelSentenceFlowManager,
                                 lifetime: Lifetime,
                                 flowContentController: FlowController,
                                 private val flowManager: FlowManager) : ReflectionBindableController(R.layout.parallel_sentence) {

  enum class State {
    ShowQuestion,
    ShowAnswer
  }

  val goodBtn = ButtonController(R.id.goodBtn, lifetime, "Good")
  val badBtn = ButtonController(R.id.badBtn, lifetime, "Bad")
  val notSureBtn = ButtonController(R.id.notSureBtn, lifetime, "Not Sure")
  val questionTextView = TextViewController(R.id.question, lifetime)
  val answerTextView = TextViewController(R.id.answer, lifetime)
  private val buttons = listOf(goodBtn, badBtn, notSureBtn)

  private val state = Property(lifetime, State.ShowQuestion)

  init {
    goodBtn.clickedSignal.subscribe(lifetime, { next() })
    badBtn.clickedSignal.subscribe(lifetime, { next() })
    notSureBtn.clickedSignal.subscribe(lifetime, { next() })

    state.forEachValue(lifetime, { state, lt ->
      if (state == State.ShowQuestion) {
        buttons.forEach { it.visible.value = Visibility.Collapsed }
        answerTextView.visible.value = Visibility.Collapsed
      } else if (state == State.ShowAnswer) {
        buttons.forEach { it.visible.value = Visibility.Visible }
        answerTextView.visible.value = Visibility.Visible
      }
    })

    parallelSentenceFlowManager.question.forEachValue(lifetime, { data, lt ->
      data!!
      state.value = State.ShowQuestion
      questionTextView.title.value = data.question
      answerTextView.title.value = data.answer
      flowContentController.setContent(this)
    })
  }

  private fun next() {
    flowManager.next()
  }

  override fun onClicked() {
    state.value = State.ShowAnswer
  }
}

