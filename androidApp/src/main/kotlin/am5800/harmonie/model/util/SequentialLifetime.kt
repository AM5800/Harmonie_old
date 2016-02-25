package am5800.harmonie.model.util

import am5800.harmonie.model.Lifetime


class SequentialLifetime(private val parentLifetime: Lifetime) {
  var current: Lifetime? = Lifetime()
  fun next(): Lifetime? {
    if (parentLifetime.isTerminated) return null
    current?.terminate()
    current = Lifetime()
    return current
  }
}