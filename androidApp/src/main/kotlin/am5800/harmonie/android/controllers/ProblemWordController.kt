package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import android.widget.TextView


class ProblemWordController(private val vm: String) : BindableController {
  override val id = R.layout.problem_word

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val textView = view.getChild<TextView>(R.id.word)

    textView.text = vm
  }
}