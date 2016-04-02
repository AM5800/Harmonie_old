package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.SequentialLifetime
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.app.vm.ObservableCollection
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView

class ListViewAdapter(private val controllers: List<BindableController>,
                      private val lifetime: Lifetime,
                      private val parentView: BindableView) : BaseAdapter() {
  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
    return parentView.createChildViewAndBind(controllers[position], lifetime)
  }

  override fun getItem(position: Int): Any? {
    return controllers[position]
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getCount(): Int {
    return controllers.size
  }
}

fun ListView.bind(lifetime: Lifetime, parentView: BindableView, collection: ObservableCollection<BindableController>) {
  val valuesLifetime = SequentialLifetime(lifetime)
  collection.changed.subscribe(lifetime, { resetAdapter(this, valuesLifetime.next(), parentView, collection) })
  resetAdapter(this, valuesLifetime.current, parentView, collection)
}

private fun resetAdapter(listView: ListView, bindingLifetime: Lifetime, view: BindableView, collection: ObservableCollection<BindableController>) {
  val controllers = collection.toList()
  listView.adapter = ListViewAdapter(controllers, bindingLifetime, view)
}