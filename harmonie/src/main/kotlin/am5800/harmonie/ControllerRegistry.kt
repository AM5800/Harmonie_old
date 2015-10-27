package am5800.harmonie

import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.viewBinding.BindableFragment
import android.support.v4.app.FragmentManager
import java.util.*

public class ControllerRegistry : ViewOpener {
    private val controllerStack = LinkedList<BindableController>()

    fun top(): BindableController {
        return controllerStack.last()
    }

    fun restoreController(layoutId: Int): BindableController {
        val result = controllerStack.last()
        if (result.id != layoutId) throw Exception("Trying to restore not the last controller")
        return controllerStack.last()
    }

    override fun bringToFront(controller: BindableController) {
        controllerStack.addLast(controller)
        val fm = fragmentManager!!
        val ft = fm.beginTransaction()
        ft.replace(R.id.main_layout, BindableFragment())
        ft.commit()
    }

    fun restore(supportFragmentManager: FragmentManager) {
        fragmentManager = supportFragmentManager
    }

    fun back() : Boolean{
        if (controllerStack.size() <= 1) return false
        controllerStack.removeLast()
        val fm = fragmentManager!!
        val ft = fm.beginTransaction()
        ft.replace(R.id.main_layout, BindableFragment())
        ft.commit()
        return true
    }

    private var fragmentManager: FragmentManager? = null

    fun start(supportFragmentManager: FragmentManager) {
        fragmentManager = supportFragmentManager

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_layout, BindableFragment())
        transaction.commit()
    }
}