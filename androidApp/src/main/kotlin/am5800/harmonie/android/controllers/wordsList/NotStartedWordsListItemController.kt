package am5800.harmonie.android.controllers.wordsList

import am5800.common.utils.Lifetime
import am5800.harmonie.android.R
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.wordsList.NotStartedWordsListItemViewModel
import android.widget.ImageButton
import android.widget.TextView


class NotStartedWordsListItemController(private val vm: NotStartedWordsListItemViewModel) : BindableController {
  override val id = R.layout.words_list_not_started
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val titleView = view.getChild<TextView>(R.id.title)
    titleView.text = vm.title

    val pullUpBtn = view.getChild<ImageButton>(R.id.pullUp)
    pullUpBtn.setOnClickListener { vm.pullUp() }

    val orderTxt = view.getChild<TextView>(R.id.order)
    orderTxt.text = "${vm.order}."
  }
}