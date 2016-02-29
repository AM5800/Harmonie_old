package utils

class SequentialLifetime(private val parentLifetime: Lifetime) {
  private val lockObject = Any()

  var current: Lifetime = Lifetime(parentLifetime)
    get() = synchronized(lockObject) { field }
    private set(value) {
      field = value
    }

  fun next(): Lifetime {
    return synchronized(lockObject) {
      current.terminate()
      current = Lifetime(parentLifetime)
      current
    }
  }
}