package am5800.harmonie.android

import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableFragment
import android.support.v4.app.FragmentManager
import java.util.*

class ControllerStack() {
  private class StackItem(val key: String?, val controller: BindableController, val replaceable: Boolean)

  private val controllerStack = LinkedList<StackItem>()

  fun top(): BindableController {
    return controllerStack.last().controller
  }

  fun restoreController(layoutId: Int): BindableController {
    val result = controllerStack.last()
    if (result.controller.id != layoutId) throw Exception("Trying to restore not the last controller")
    return controllerStack.last().controller
  }

  fun push(controller: BindableController, key: String?) {
    push(StackItem(key, controller, false))
  }

  private fun push(item: StackItem) {
    if (controllerStack.lastOrNull()?.replaceable == true) controllerStack.removeLast()
    if (item.key == null || controllerStack.lastOrNull()?.key != item.key) controllerStack.addLast(item)

    val fm = fragmentManager ?: return
    val ft = fm.beginTransaction()
    ft.replace(R.id.main_layout, BindableFragment())
    ft.commit()
  }

  fun pushReplaceable(controller: BindableController) {
    push(StackItem(null, controller, true))
  }

  fun initialize(fm: FragmentManager) {
    fragmentManager = fm
  }

  fun back(): Boolean {
    if (controllerStack.size <= 1) return false
    controllerStack.removeLast()
    val fm = fragmentManager!!
    val ft = fm.beginTransaction()
    ft.replace(R.id.main_layout, BindableFragment())
    ft.commit()
    return true
  }

  private var fragmentManager: FragmentManager? = null
}