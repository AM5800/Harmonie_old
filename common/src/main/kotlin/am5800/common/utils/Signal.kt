package am5800.common.utils

import java.util.*

class Signal<T>(private val lifetime: Lifetime) {
  init {
    lifetime.addAction { -> handlers.clear() }
  }

  private val handlers = ArrayList<(T) -> Unit>()

  fun fire(payload: T) {
    for (handler in handlers) handler(payload)
  }

  fun subscribe(lt: Lifetime, handler: (T) -> Unit) {
    if (lifetime.isTerminated) return
    if (lt.isTerminated) return
    handlers.add(handler)
    lt.addAction { -> handlers.remove(handler) }
  }
}

fun Signal<Unit>.fire() {
  this.fire(Unit)
}