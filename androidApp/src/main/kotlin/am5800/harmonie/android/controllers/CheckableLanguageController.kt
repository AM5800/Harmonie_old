package am5800.harmonie.android.controllers

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.CheckableLanguageViewModel
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

class CheckableLanguageController(override val id: Int, private val viewModel: CheckableLanguageViewModel) : BindableController {
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val imageView = view.getChild<ImageView>(R.id.flagImage)
    imageView.setImageResource(getResource(viewModel.language))
    val textView = view.getChild<TextView>(R.id.languageTextView)
    textView.text = viewModel.title
    val checkBox = view.getChild<CheckBox>(R.id.languageCheckbox)
    checkBox.bindCheckedTwoWay(bindingLifetime, view, viewModel.checked)
  }

  override fun onClicked() {
    viewModel.checked.value = !viewModel.checked.value!!
  }

  private fun getResource(language: Language): Int {
    return when (language) {
      Language.Russian -> R.drawable.ru
      Language.German -> R.drawable.de
      Language.Japanese -> R.drawable.jp
      Language.English -> R.drawable.en
    }
  }
}