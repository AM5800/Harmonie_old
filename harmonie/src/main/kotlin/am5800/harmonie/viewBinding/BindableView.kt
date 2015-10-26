package am5800.harmonie.viewBinding

import am5800.harmonie.model.Lifetime
import android.app.Activity
import android.view.View

public interface BindableView {
    val activity : Activity
    fun <T> getChild(layoutId: Int): T
    fun createViewAndBind(vm: BindableController, bindingLifetime: Lifetime): View

    fun runOnUiThread(function: () -> Unit)
}