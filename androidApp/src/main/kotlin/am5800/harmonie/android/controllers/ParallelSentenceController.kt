package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.model.repetition.AttemptScore
import am5800.harmonie.app.vm.ParallelSentenceViewModel
import am5800.harmonie.app.vm.ToggleableWordViewModel
import am5800.harmonie.app.vm.WordViewModel
import android.graphics.Color
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import org.apmem.tools.layouts.FlowLayout

class ParallelSentenceController(lifetime: Lifetime,
                                 flowContentController: FlowController,
                                 private val vm: ParallelSentenceViewModel
) : BindableController {

  override val id: Int = R.layout.parallel_sentence
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    super.bind(view, bindingLifetime)

    val answer = view.getChild<TextView>(R.id.answer)
    answer.bindText(bindingLifetime, view, vm.answer)
    answer.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility)

    val nextBtn = view.getChild<Button>(R.id.continueBtn)
    nextBtn.bindOnClick(bindingLifetime, { vm.next() })

    val flowLayout = view.getChild<FlowLayout>(R.id.question)
    vm.question.bind(bindingLifetime, {
      flowLayout.removeAllViews()
      for (childVm in it.newValue!!) {
        val wordView = TextView(view.activity)
        setupWordView(wordView, childVm, bindingLifetime)
        flowLayout.addView(wordView)
      }
    })
  }

  private fun setupWordView(wordView: TextView, childVm: WordViewModel, bindingLifetime: Lifetime) {
    wordView.text = childVm.text

    if (childVm is ToggleableWordViewModel) {
      wordView.setOnClickListener({ childVm.toggle() })
      childVm.state.bind(bindingLifetime, {
        if (it.newValue == AttemptScore.Ok) wordView.setTextColor(Color.BLACK)
        else wordView.setTextColor(Color.RED)
      })
    }

    wordView.setPadding(if (childVm.needSpaceBefore) 16 else 0, 8, 0, 8)
    wordView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20.0f)
  }

  init {
    vm.activationRequired.subscribe(lifetime, { flowContentController.setContent(this) })
  }
}

