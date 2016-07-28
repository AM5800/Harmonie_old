package am5800.harmonie.android.controllers.wordsList

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import android.widget.TextView


class WordsListItemController(private val title: String) : BindableController {
  override val id: Int
    get() = R.layout.words_list_item

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val textView = view.getChild<TextView>(R.id.lemmaPresentation)
    textView.text = title
  }
}