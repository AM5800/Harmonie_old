package am5800.harmonie.android.viewBinding

import am5800.common.utils.Lifetime
import android.app.Activity
import android.view.View

interface BindableView {
  val activity: Activity
  fun <T> getChild(layoutId: Int): T
  fun createViewAndBind(vm: BindableController, bindingLifetime: Lifetime): View

  fun runOnUiThread(function: () -> Unit)
}