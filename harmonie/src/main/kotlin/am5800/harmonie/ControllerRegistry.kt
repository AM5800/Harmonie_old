package am5800.harmonie

import am5800.harmonie.model.Lifetime
import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.viewBinding.BindableFragment
import android.support.v4.app.FragmentManager
import java.util.*

public class ControllerRegistry {
    private val knownVms = LinkedList<BindableController>()
    private val vmStack = LinkedList<BindableController>()
    private var defaultVm : BindableController? = null

    public fun registerKnownController(vm : BindableController, lifetime : Lifetime) {
        knownVms.addLast(vm)
        lifetime.addAction {
            knownVms.remove(vm)
        }
    }

    fun top(): BindableController {
        return vmStack.last()
    }

    fun restoreVm(layoutId: Int): BindableController {
        val result = knownVms.first { it.id == layoutId }
        return result
    }

    fun bringToFront(vm: BindableController) {
        vmStack.addLast(vm)
        val fm = fragmentManager!!
        val ft = fm.beginTransaction()
        ft.replace(R.id.main_layout, BindableFragment())
        ft.commit()
    }

    fun restore(supportFragmentManager: FragmentManager) {
        fragmentManager = supportFragmentManager
    }

    fun setDefaultVm(vm : BindableController) {
        defaultVm = vm
    }

    fun back() : Boolean{
        if (vmStack.size() <= 1) return false
        vmStack.removeLast()
        val fm = fragmentManager!!
        val ft = fm.beginTransaction()
        ft.replace(R.id.main_layout, BindableFragment())
        ft.commit()
        return true
    }

    private var fragmentManager: FragmentManager? = null

    fun start(supportFragmentManager: FragmentManager) {
        vmStack.addLast(defaultVm!!) // Default vm should be always set

        fragmentManager = supportFragmentManager

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.main_layout, BindableFragment())
        transaction.commit()
    }
}