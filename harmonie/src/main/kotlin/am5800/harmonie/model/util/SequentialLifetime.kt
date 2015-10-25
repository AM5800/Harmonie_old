package am5800.harmonie.model.util

import am5800.harmonie.model.Lifetime


public class SequentialLifetime(private val parentLifetime: Lifetime) {
    public var current: Lifetime? = Lifetime()
    fun next(): Lifetime? {
        if (parentLifetime.isTerminated) return null
        current?.terminate()
        current = Lifetime()
        return current
    }
}