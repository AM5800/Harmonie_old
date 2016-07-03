package am5800.harmonie.android.controllers

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.controllers.util.bindCheckedTwoWay
import am5800.harmonie.android.controllers.util.bindText
import am5800.harmonie.android.controllers.util.bindVisibility
import am5800.harmonie.android.toAndroidVisibility
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.CheckableLanguageViewModel
import am5800.harmonie.app.vm.CheckableLanguageWithCounterViewModel
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

class CheckableLanguageController(override val id: Int, private val viewModel: CheckableLanguageViewModel) : BindableController {
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val thisView = view.getChild<View>(R.id.checkable_language_container)
    thisView.bindVisibility(bindingLifetime, view, viewModel.visible, Visibility.Collapsed)

    val imageView = view.getChild<ImageView>(R.id.flagImage)
    imageView.setImageResource(getResource(viewModel.language))

    val textView = view.getChild<TextView>(R.id.languageTextView)
    textView.text = viewModel.title

    val checkBox = view.getChild<CheckBox>(R.id.languageCheckbox)
    checkBox.bindCheckedTwoWay(bindingLifetime, view, viewModel.checked)

    val countTextView = view.getChild<TextView>(R.id.countTextView)
    if (viewModel is CheckableLanguageWithCounterViewModel) {
      countTextView.bindText(bindingLifetime, view, viewModel.countText)
    } else {
      countTextView.visibility = Visibility.Collapsed.toAndroidVisibility()
    }
  }

  override fun onClicked() {
    viewModel.checked.value = !viewModel.checked.value
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