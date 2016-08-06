package am5800.harmonie.android.controllers.wordsList

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.wordsList.OnLearningWordsListItemViewModel
import am5800.harmonie.app.vm.wordsList.Status
import android.widget.TextView

class OnLearningWordsListItemController(private val vm: OnLearningWordsListItemViewModel) : BindableController {
  override val id = R.layout.words_list_item_on_learning
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val status = view.getChild<TextView>(R.id.dueStatus)

    status.text = vm.dueStatusString

    val title = view.getChild<TextView>(R.id.title)
    title.text = vm.title

    val colorId = when (vm.status) {

      Status.Ok -> R.color.okColor
      Status.Warning -> R.color.warningColor
      Status.Error -> R.color.errorColor
    }

    status.setTextColor(view.activity.resources.getColor(colorId))
  }
}