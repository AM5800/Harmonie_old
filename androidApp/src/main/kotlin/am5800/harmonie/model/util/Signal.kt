package am5800.harmonie.model.util

import am5800.harmonie.model.Lifetime
import java.util.*

class Signal <T>(private val lifetime: Lifetime) {
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
    lt.addAction { -> handlers.remove (handler) }
  }
}