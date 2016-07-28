package am5800.harmonie.android.controllers.wordsList

import am5800.common.utils.Lifetime
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.controllers.util.ListViewController
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.android.viewBinding.FragmentController
import am5800.harmonie.app.vm.wordsList.WordsListViewModel


class WordsListController(private val vm: WordsListViewModel,
                          controllerStack: ControllerStack,
                          lifetime: Lifetime) : BindableController, FragmentController {
  init {
    vm.activationRequested.subscribe(lifetime, { controllerStack.push(this, javaClass.name) })
  }

  override val id = R.layout.words_list
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    ListViewController.bind(R.id.wordsList, bindingLifetime, view, vm.items, {
      WordsListItemController(it.title)
    })
  }
}