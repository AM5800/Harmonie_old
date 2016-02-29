package utils

class SequentialLifetime(private val parentLifetime: Lifetime) {
  // TODO: lifetime nesting
  var current: Lifetime? = Lifetime()
  fun next(): Lifetime? {
    if (parentLifetime.isTerminated) return null
    current?.terminate()
    current = Lifetime()
    return current
  }
}