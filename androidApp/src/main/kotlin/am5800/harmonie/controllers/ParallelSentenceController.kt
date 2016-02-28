package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.controllers.defaultControls.ButtonController
import am5800.harmonie.controllers.defaultControls.TextViewController
import am5800.harmonie.model.newest.FlowManager
import am5800.harmonie.model.newest.ParallelSentenceFlowManager
import am5800.harmonie.viewBinding.ReflectionBindableController
import utils.Lifetime

class ParallelSentenceController(parallelSentenceFlowManager: ParallelSentenceFlowManager,
                                 lifetime: Lifetime,
                                 flowContentController: FlowController,
                                 flowManager: FlowManager) : ReflectionBindableController(R.layout.parallel_sentence) {

  val goodBtn = ButtonController(R.id.goodBtn, lifetime, "Goooood")
  val questionTextView = TextViewController(R.id.question, lifetime)

  init {
    goodBtn.clickedSignal.subscribe(lifetime, { flowManager.next() })

    parallelSentenceFlowManager.presentationRequested.subscribe(lifetime, { data ->
      questionTextView.title.value = data.question
      flowContentController.setContent(this)
    })
  }
}

