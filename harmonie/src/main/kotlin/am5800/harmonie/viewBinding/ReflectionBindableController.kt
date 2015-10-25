package am5800.harmonie.viewBinding

import am5800.harmonie.model.Lifetime
import am5800.harmonie.viewBinding.BindableView
import am5800.harmonie.viewBinding.BindableController

public open class ReflectionBindableController(override val id : Int) : BindableController {
    override fun bind(view: BindableView, bindingLifetime: Lifetime) {
        val methods = javaClass.getDeclaredMethods()
        for (method in methods) {
            val clazz = method.getReturnType()
            if (!javaClass<BindableController>().isAssignableFrom(clazz)) continue

            val result = method.invoke(this) as BindableController
            result.bind(view, bindingLifetime)
        }
    }
}