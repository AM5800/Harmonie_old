package am5800.harmonie.android.viewBinding

import am5800.common.utils.Lifetime
import am5800.harmonie.android.ControllerStack
import am5800.harmonie.android.MainActivity
import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*

class BindableFragment : Fragment() {
  private val fragmentLifetime: Lifetime = Lifetime()
  private var controllers: ControllerStack? = null
  private var layoutId: Int = -1
  private val idTag = "LAYOUT_ID"
  private var currentController: FragmentController? = null

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val registry = controllers!!
    val controller =
        if (savedInstanceState == null) {
          val result = registry.top()
          layoutId = result.id
          result
        } else {
          layoutId = savedInstanceState.getInt(idTag)
          registry.restoreController(layoutId)
        }

    currentController = controller
    val bindableView = BindableViewImpl(controller.id, activity)
    controller.bind(bindableView, fragmentLifetime)
    controller.onActivated()
    if (controller is ControllerWithMenu) {
      setHasOptionsMenu(controller.menuItems.value.any() == true)
    }
    return bindableView.view
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(outState)
    outState!!.putInt(idTag, layoutId)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
    val controller = currentController!!
    if (controller !is ControllerWithMenu) return
    controller.menuItems.onChange(fragmentLifetime, {
      menu.clear()
      for (item in it.newValue) {
        val menuItem = menu.add(item.title.value)
        menuItem.setOnMenuItemClickListener {
          item.onClick()
          true
        }
      }
    })
  }

  override fun onAttach(activity: Activity?) {
    super.onAttach(activity)
    if (activity !is MainActivity) return

    controllers = activity.controllerStack
    activity.mainActivityLifetime?.addAction { fragmentLifetime.terminate() }
  }

  override fun onDetach() {
    super.onDetach()
    fragmentLifetime.terminate()
  }
}