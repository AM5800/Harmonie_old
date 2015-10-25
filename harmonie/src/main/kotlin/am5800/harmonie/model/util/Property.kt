package am5800.harmonie.model.util

import am5800.harmonie.model.Lifetime
import java.util.ArrayList

public class PropertyChangedArg<T>(private val old: T, public val newValue: T?, public val hasOld: Boolean) {
    val oldValue: T
        get() = if (!hasOld) throw Exception("Old value does not exist") else old

    var handled = false
}

public class Property<T>(value: T?) {
    private val binders: ArrayList<(PropertyChangedArg<T?>) -> Unit> = ArrayList()
    var value: T? = value
        get() = $value
        set(v) {
            if ($value == v) return
            val arg = PropertyChangedArg($value, v, true)
            $value = v
            binders.forEach { b -> if (!arg.handled) b(arg) }
        }

    fun bind(lifetime: Lifetime, binder: (PropertyChangedArg<T?>) -> Unit) {
        binders.add(binder)
        lifetime.addAction { binders.remove (binder) }
        binder(PropertyChangedArg(null, $value, false))
    }

    fun bindNotNull(lifetime: Lifetime, binder: (T) -> Unit) {
        bind(lifetime, { arg -> if (arg.newValue != null) binder(arg.newValue) })
    }

    fun bindHasOld(lifetime: Lifetime, binder: (T?) -> Unit) {
        bind(lifetime, { arg ->
            if (arg.hasOld) binder(arg.newValue)
        })
    }
}