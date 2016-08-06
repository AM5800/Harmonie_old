package am5800.harmonie.android.controllers.wordsList

import am5800.common.utils.Lifetime
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.R
import am5800.harmonie.android.controllers.util.ListViewController
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.android.viewBinding.FragmentController
import am5800.harmonie.app.vm.wordsList.NotStartedWordsListItemViewModel
import am5800.harmonie.app.vm.wordsList.OnLearningWordsListItemViewModel
import am5800.harmonie.app.vm.wordsList.SeparatorWordsListItemViewModel
import am5800.harmonie.app.vm.wordsList.WordsListViewModel
import android.widget.ListView
import android.widget.SearchView


class WordsListController(private val vm: WordsListViewModel,
                          controllerStack: ControllerStack,
                          lifetime: Lifetime) : BindableController, FragmentController {
  init {
    vm.activationRequested.subscribe(lifetime, { controllerStack.push(this, javaClass.name) })
  }

  override fun onActivated() {
    vm.onActivated()
  }

  override val id = R.layout.words_list
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {

    val listView = view.getChild<ListView>(R.id.wordsList)

    ListViewController.bind(listView, bindingLifetime, view, vm.items, {
      if (it is NotStartedWordsListItemViewModel) NotStartedWordsListItemController(it)
      else if (it is SeparatorWordsListItemViewModel) WordsListItemController(it.title)
      else if (it is OnLearningWordsListItemViewModel) WordsListItemController(it.title)
      else throw Exception("Unsupported ViewModel: " + it.javaClass.name)
    })

    vm.scrollPosition.onChange(bindingLifetime, {
      listView.setSelection(vm.scrollPosition.value - 3)
    })

    val searchView = view.getChild<SearchView>(R.id.searchView)
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String): Boolean {
        vm.search(query)
        return false
      }

      override fun onQueryTextChange(newText: String): Boolean {
        vm.search(newText)
        return false
      }
    })
  }
}