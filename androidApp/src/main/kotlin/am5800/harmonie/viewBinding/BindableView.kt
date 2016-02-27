package am5800.harmonie.viewBinding

import Lifetime
import android.app.Activity
import android.view.View

interface BindableView {
  val activity: Activity
  fun <T> getChild(layoutId: Int): T
  fun createViewAndBind(vm: BindableController, bindingLifetime: Lifetime): View

  fun runOnUiThread(function: () -> Unit)
}