package am5800.harmonie.model

import java.util.ArrayList

public class Lifetime {
    private val actions = ArrayList<() -> Unit>()

    var isTerminated: Boolean = false

    fun terminate() {
        isTerminated = true
        for (action in actions) {
            action()
        }
    }

    fun addAction(action: () -> Unit) {
        if (isTerminated) return
        actions.add (action)
    }
}