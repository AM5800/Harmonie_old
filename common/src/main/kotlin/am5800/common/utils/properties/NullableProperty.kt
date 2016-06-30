package am5800.common.utils.properties

import am5800.common.utils.Lifetime
import am5800.common.utils.SequentialLifetime
import java.util.*

class NullableProperty<T : Any>(lifetime: Lifetime, initialValue: T? = null) : NullableReadonlyProperty<T> {

  private val handlers: ArrayList<(NullablePropertyChangedArg<T>) -> Unit> = ArrayList()

  init {
    lifetime.addAction { handlers.clear() }
  }

  override fun forEachValue(lifetime: Lifetime, handler: (T?, Lifetime) -> Unit) {
    onChange(lifetime, { args ->
      handler(args.newValue, valueLifetime.current)
    })
  }

  override fun onChange(lifetime: Lifetime, handler: (NullablePropertyChangedArg<T>) -> Unit) {
    handlers.add(handler)
    lifetime.addAction { handlers.remove(handler) }
    handler(NullablePropertyChangedArg(null, value, false))
  }

  private val valueLifetime = SequentialLifetime(lifetime)

  override var value: T? = initialValue
    get() = field
    set(newValue) {
      if (field == newValue) return
      valueLifetime.next()
      val arg = NullablePropertyChangedArg(field, newValue, true)
      field = newValue
      handlers.toList().forEach { b -> b(arg) }
    }

}