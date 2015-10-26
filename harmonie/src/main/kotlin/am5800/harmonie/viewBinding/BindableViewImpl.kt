package am5800.harmonie.viewBinding

import am5800.harmonie.model.Lifetime
import android.app.Activity
import android.view.LayoutInflater
import android.view.View


public class BindableViewImpl(private val layoutInflater: LayoutInflater, layoutId: Int, public override val activity : Activity) : BindableView {
    override fun runOnUiThread(function: () -> Unit) {
        activity.runOnUiThread(function)
    }

    val view: View = layoutInflater.inflate(layoutId, null)

    override fun createViewAndBind(vm: BindableController, bindingLifetime: Lifetime): View {
        val result = BindableViewImpl(layoutInflater, vm.id, activity)
        vm.bind(result, bindingLifetime)
        return result.view
    }

    override fun <T> getChild(layoutId: Int): T {
        return view.findViewById(layoutId) as T
    }
}