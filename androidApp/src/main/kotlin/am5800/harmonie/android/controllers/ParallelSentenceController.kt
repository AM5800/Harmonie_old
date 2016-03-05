package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.controllers.defaultControls.ButtonController
import am5800.harmonie.android.controllers.defaultControls.TextViewController
import am5800.harmonie.android.viewBinding.ReflectionBindableController
import am5800.harmonie.app.model.flow.ParallelSentenceUserScore
import am5800.harmonie.app.vm.ParallelSentenceViewModel

class ParallelSentenceController(lifetime: Lifetime,
                                 flowContentController: FlowController,
                                 private val vm: ParallelSentenceViewModel
) : ReflectionBindableController(R.layout.parallel_sentence) {

  val goodBtn = ButtonController(R.id.goodBtn, lifetime, "Good")
  val badBtn = ButtonController(R.id.badBtn, lifetime, "Bad")
  val notSureBtn = ButtonController(R.id.notSureBtn, lifetime, "Not Sure")
  val questionTextView = TextViewController(R.id.question, lifetime)
  val answerTextView = TextViewController(R.id.answer, lifetime)
  private val buttons = listOf(goodBtn, badBtn, notSureBtn)


  init {
    goodBtn.clickedSignal.subscribe(lifetime, { vm.submitAnswer(ParallelSentenceUserScore.Good) })
    badBtn.clickedSignal.subscribe(lifetime, { vm.submitAnswer(ParallelSentenceUserScore.Bad) })
    notSureBtn.clickedSignal.subscribe(lifetime, { vm.submitAnswer(ParallelSentenceUserScore.NotSure) })

    questionTextView.title.bind(lifetime, vm.question)
    answerTextView.title.bind(lifetime, vm.answer)
    vm.activationRequired.subscribe(lifetime, { flowContentController.setContent(this) })

    vm.answerGroupVisibility.forEachValue(lifetime, { visibility, lt ->
      buttons.forEach { it.visible.value = visibility }
      answerTextView.visible.value = visibility
    })
  }

  override fun onClicked() {
    vm.showAnswer()

  }
}

