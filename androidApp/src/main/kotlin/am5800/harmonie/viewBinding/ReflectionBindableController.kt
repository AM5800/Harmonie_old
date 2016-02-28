package am5800.harmonie.viewBinding

import utils.Lifetime

open class ReflectionBindableController(override val id: Int) : BindableController {
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val methods = javaClass.declaredMethods
    for (method in methods) {
      val clazz = method.returnType
      if (!BindableController::class.java.isAssignableFrom(clazz)) continue

      val result = method.invoke(this) as BindableController
      result.bind(view, bindingLifetime)
    }
  }
}