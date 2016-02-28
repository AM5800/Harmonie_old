package am5800.harmonie

import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.viewBinding.BindableFragment
import android.support.v4.app.FragmentManager
import java.util.*

class ControllerStack {
  private val controllerStack = LinkedList<BindableController>()

  fun top(): BindableController {
    return controllerStack.last()
  }

  fun restoreController(layoutId: Int): BindableController {
    val result = controllerStack.last()
    if (result.id != layoutId) throw Exception("Trying to restore not the last controller")
    return controllerStack.last()
  }

  fun bringToFront(controller: BindableController) {
    controllerStack.addLast(controller)
    val fm = fragmentManager ?: return
    val ft = fm.beginTransaction()
    ft.replace(R.id.main_layout, BindableFragment())
    ft.commit()
  }

  fun restore(fm: FragmentManager) {
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

  fun start(supportFragmentManager: FragmentManager, rootController: BindableController) {
    controllerStack.addLast(rootController)
    fragmentManager = supportFragmentManager

    val transaction = supportFragmentManager.beginTransaction()
    transaction.add(R.id.main_layout, BindableFragment())
    transaction.commit()
  }
}