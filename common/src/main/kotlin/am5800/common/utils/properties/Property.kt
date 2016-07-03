package am5800.common.utils.properties

import am5800.common.utils.Lifetime
import am5800.common.utils.SequentialLifetime
import java.util.*

class Property<T : Any>(val lifetime: Lifetime, initialValue: T) : ReadonlyProperty<T> {
  init {
    lifetime.addAction { handlers.clear() }
  }

  private val handlers: ArrayList<(PropertyChangedArg<T>) -> Unit> = ArrayList()
  private val valueLifetime = SequentialLifetime(lifetime)

  override var value: T = initialValue
    get() = field
    set(newValue) {
      if (field == newValue) return
      valueLifetime.next()
      val arg: PropertyChangedArg<T> = PropertyChangedArg(field, newValue)
      field = newValue
      handlers.toList().forEach { b -> b(arg) }
    }

  override fun onChange(lifetime: Lifetime, handler: (PropertyChangedArg<T>) -> Unit) {
    handlers.add(handler)
    lifetime.addAction { handlers.remove(handler) }
    handler(PropertyChangedArg(null, value))
  }

  override fun forEachValue(lifetime: Lifetime, handler: (T, Lifetime) -> Unit) {
    onChange(lifetime, { args ->
      handler(args.newValue, valueLifetime.current)
    })
  }
}

