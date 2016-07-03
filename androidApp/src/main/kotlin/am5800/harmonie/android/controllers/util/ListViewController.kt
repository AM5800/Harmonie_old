package am5800.harmonie.android.controllers.util

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.ReadonlyProperty
import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableView
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView

class ListViewController {
  private class ListViewAdapter(private val controllers: List<BindableController>,
                                private val rootView: BindableView,
                                private val valueLifetime: Lifetime) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
      val controller = controllers[position]
      return rootView.createChildViewAndBind(controller, valueLifetime, null)
    }

    override fun getItem(position: Int): BindableController {
      return controllers[position]
    }

    override fun getItemId(position: Int): Long {
      return 0
    }

    override fun getCount(): Int {
      return controllers.size
    }
  }

  companion object {
    fun <T : Any> bind(listViewId: Int,
                       lifetime: Lifetime,
                       view: BindableView,
                       items: ReadonlyProperty<Collection<T>>,
                       controllerFactory: (T) -> BindableController) {
      val listView = view.getChild<ListView>(listViewId)
      items.forEachValue(lifetime, { vms, valueLifetime ->
        val controllers = vms.map { controllerFactory(it) }
        listView.adapter = ListViewAdapter(controllers, view, valueLifetime)
      })
    }
  }
}