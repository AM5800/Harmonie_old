package am5800.harmonie.android.controllers.wordsList

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.wordsList.OnLearningWordsListItemViewModel
import android.widget.TextView

class OnLearningWordsListItemController(private val vm: OnLearningWordsListItemViewModel) : BindableController {
  override val id = R.layout.words_list_item_on_learning
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val status = view.getChild<TextView>(R.id.dueStatus)

    status.text = vm.dueStatus

    val title = view.getChild<TextView>(R.id.title)
    title.text = vm.title
  }
}