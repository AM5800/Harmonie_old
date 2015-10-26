package am5800.harmonie.viewBinding

import am5800.harmonie.ControllerRegistry
import am5800.harmonie.MainActivity
import am5800.harmonie.model.Lifetime
import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

public class BindableFragment : Fragment() {
    private val fragmentLifetime: Lifetime = Lifetime()
    private var controllerRegistry: ControllerRegistry? = null
    private var layoutId : Int = -1
    private val idTag = "LAYOUT_ID"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val registry = controllerRegistry!!
        val vm =
            if (savedInstanceState == null) {
                val result = registry.top()
                layoutId = result.id
                result
            }
            else {
                layoutId = savedInstanceState.getInt(idTag)
                registry.restoreVm(layoutId)
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

        controllerRegistry = activity.controllerRegistry
        activity.mainActivityLifetime?.addAction { fragmentLifetime.terminate () }
    }

    override fun onDetach() {
        super.onDetach()
        fragmentLifetime.terminate()
    }
}