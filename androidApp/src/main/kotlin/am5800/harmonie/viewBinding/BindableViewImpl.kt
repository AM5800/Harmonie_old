package am5800.harmonie.viewBinding

import Lifetime
import android.app.Activity
import android.view.LayoutInflater
import android.view.View


class BindableViewImpl(private val layoutInflater: LayoutInflater, layoutId: Int, override val activity: Activity) : BindableView {
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