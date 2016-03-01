package am5800.common.componentContainer

import am5800.common.utils.Lifetime
import java.util.*


class ComponentContainer(val lifetime: Lifetime, val parent: ComponentContainer?) {
  private val _components = ArrayList<Any>()

  init {
    register(lifetime)
    parent?.register(this)
    lifetime.addAction { _components.clear() }
  }

  val components: List<Any>
    get() = _components


  fun register(component: Any) {
    _components.add(component)
  }
}

inline fun <reified T> ComponentContainer.getComponent(): T {
  var container: ComponentContainer? = this
  while (container != null) {
    val result = container.components.singleOrNull { it is T } as? T
    if (result != null) return result
    container = container.parent
  }

  throw Exception("Component not found" + T::class.qualifiedName)
}
