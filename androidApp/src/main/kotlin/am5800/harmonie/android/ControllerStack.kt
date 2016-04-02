package am5800.harmonie.android

import am5800.harmonie.android.viewBinding.BindableController
import am5800.harmonie.android.viewBinding.BindableFragment
import am5800.harmonie.app.model.logging.LoggerProvider
import android.support.v4.app.FragmentManager
import java.util.*

class ControllerStack(loggerProvider: LoggerProvider) {
  private val logger = loggerProvider.getLogger(javaClass)

  private val controllerStack = LinkedList<Pair<String, BindableController>>()

  fun top(): BindableController {
    return controllerStack.last().second
  }

  fun restoreController(layoutId: Int): BindableController {
    val result = controllerStack.last()
    if (result.second.id != layoutId) throw Exception("Trying to restore not the last controller")
    return controllerStack.last().second
  }

  fun push(controller: BindableController, key: String) {
    logger.info("bringToFront: $key")
    if (controllerStack.isEmpty() || controllerStack.last().first != key) {
      controllerStack.addLast(Pair(key, controller))
    }

    val fm = fragmentManager ?: return
    val ft = fm.beginTransaction()
    ft.replace(R.id.main_layout, BindableFragment())
    ft.commit()
  }

  fun restore(fm: FragmentManager) {
    logger.info("Restored")
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

  fun start(supportFragmentManager: FragmentManager, rootController: BindableController, key: String) {
    logger.info("Started")
    controllerStack.addLast(Pair(key, rootController))
    fragmentManager = supportFragmentManager

    val transaction = supportFragmentManager.beginTransaction()
    transaction.add(R.id.main_layout, BindableFragment())
    transaction.commit()
  }

  fun setRoot(controller: BindableController, key: String) {
    controllerStack.clear()
    push(controller, key)
  }
}