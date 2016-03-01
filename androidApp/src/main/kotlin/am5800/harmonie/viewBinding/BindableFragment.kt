package am5800.harmonie.viewBinding

import am5800.common.utils.Lifetime
import am5800.harmonie.ControllerStack
import am5800.harmonie.MainActivity
import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class BindableFragment : Fragment() {
  private val fragmentLifetime: Lifetime = Lifetime()
  private var controllerRegistry: ControllerStack? = null
  private var layoutId: Int = -1
  private val idTag = "LAYOUT_ID"

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val registry = controllerRegistry!!
    val vm =
            if (savedInstanceState == null) {
              val result = registry.top()
              layoutId = result.id
              result
            } else {
              layoutId = savedInstanceState.getInt(idTag)
              registry.restoreController(layoutId)
            }

    val bindableView = BindableViewImpl(inflater!!, vm.id, activity)
    vm.bind(bindableView, fragmentLifetime)
    vm.onActivated()
    return bindableView.view
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(outState)
    outState!!.putInt(idTag, layoutId)
  }

  override fun onAttach(activity: Activity?) {
    super.onAttach(activity)
    if (activity !is MainActivity) return

    controllerRegistry = activity.controllerStack
    activity.mainActivityLifetime?.addAction { fragmentLifetime.terminate () }
  }

  override fun onDetach() {
    super.onDetach()
    fragmentLifetime.terminate()
  }
}