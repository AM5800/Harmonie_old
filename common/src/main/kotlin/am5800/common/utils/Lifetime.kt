package am5800.common.utils

import java.io.Closeable
import java.util.*

class TerminatedLifetimeException : Exception("Lifetime is already terminated")

class Lifetime(parentLifetime: Lifetime? = null) : Closeable {
  private val lockObject = Any()

  override fun close() {
    terminate()
  }

  private var _isTerminated: Boolean = false
  var isTerminated: Boolean = false
    get() = synchronized(lockObject) { _isTerminated }

  init {
    if (parentLifetime != null) {
      _isTerminated = !parentLifetime.tryAddAction { terminate() }
    }
  }

  private val actions = ArrayList<() -> Unit>()

  fun terminate() {
    var actionsToExecute: List<() -> Unit>? = null

    synchronized(lockObject) {
      if (_isTerminated) return
      _isTerminated = true
      actionsToExecute = actions.toList()
      actions.clear()
    }

    if (actionsToExecute == null) return
    for (action in actionsToExecute!!) {
      action()
    }
  }

  fun addAction(action: () -> Unit) {
    synchronized(lockObject) {
      if (isTerminated) throw TerminatedLifetimeException()
      actions.add(action)
    }
  }

  fun tryAddAction(action: () -> Unit): Boolean {
    return synchronized(lockObject) {
      if (_isTerminated) return false

      actions.add(action)
      true
    }
  }

  fun <T> execute(function: () -> T): T {
    return synchronized(lockObject) {
      if (isTerminated) throw TerminatedLifetimeException()
      function()
    }
  }
}