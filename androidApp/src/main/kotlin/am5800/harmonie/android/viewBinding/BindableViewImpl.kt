package am5800.harmonie.android.viewBinding

import am5800.common.utils.Lifetime
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class BindableViewImpl(layoutId: Int, override val activity: Activity, parent: ViewGroup? = null) : BindableView {
  private val layoutInflater: LayoutInflater = LayoutInflater.from(activity)
  override fun runOnUiThread(function: () -> Unit) {
    activity.runOnUiThread(function)
  }

  val view: View = layoutInflater.inflate(layoutId, parent)

  override fun createChildViewAndBind(controller: BindableController, bindingLifetime: Lifetime, parent: ViewGroup?): View {
    val result = BindableViewImpl(controller.id, activity, parent)
    result.view.setOnClickListener({ controller.onClicked() })
    controller.bind(result, bindingLifetime)
    return result.view
  }

  override fun <T> getChild(layoutId: Int): T {
    if (view.id == layoutId) return view as T
    return view.findViewById(layoutId) as T
  }
}