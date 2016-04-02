package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.ReadonlyProperty
import am5800.common.utils.Signal

class ObservableCollection<T>(val lifetime: Lifetime) : Collection<T> {
  private val instance = mutableListOf<Pair<T, Lifetime>>()
  val changed = Signal<Unit>(lifetime)

  fun add(callback: (Lifetime) -> T) {
    val childLifetime = Lifetime(lifetime)
    val child = callback(childLifetime)
    val pair = Pair(child, childLifetime)
    instance.add(pair)
    lifetime.addAction { instance.remove(pair) }
    changed.fire(Unit)
  }

  fun clear() {
    val copy = instance.toList()
    instance.clear()
    copy.forEach { it.second.terminate() }
    changed.fire(Unit)
  }

  override fun iterator(): Iterator<T> {
    return instance.map { it.first }.iterator()
  }

  fun remove(element: T): Boolean {
    val result = instance.firstOrNull() { it.first == element }
    if (result != null) {
      result.second.terminate()
      return true
    }
    return false
  }

  override val size: Int
    get() = instance.size

  override fun contains(element: T): Boolean {
    throw UnsupportedOperationException()
  }

  override fun containsAll(elements: Collection<T>): Boolean {
    throw UnsupportedOperationException()
  }

  override fun isEmpty(): Boolean {
    return instance.isEmpty()
  }
}

fun <TSrc : Any, TDst : Any> ObservableCollection<TSrc>.toProperty(aggregateFunction: (List<TSrc>) -> TDst): ReadonlyProperty<TDst> {
  val result = Property<TDst>(this.lifetime, null)
  val handler: (Unit) -> Unit = {
    result.value = aggregateFunction(this.toList())
  }
  this.changed.subscribe(this.lifetime, handler)
  handler(Unit)

  return result
}


fun <TSrc : Any, TDst : Any> ObservableCollection<TSrc>.mapObservable(f: (TSrc) -> TDst): ObservableCollection<TDst> {
  val result = ObservableCollection<TDst>(this.lifetime)

  val handler: (Unit) -> Unit = {
    result.clear()
    this.forEach { item -> result.add { f(item) } }
  }

  this.changed.subscribe(this.lifetime, handler)
  handler(Unit)

  return result
}


fun <T : Any> ObservableCollection<T>.filterObservable(f: (T) -> Boolean): ObservableCollection<T> {
  val result = ObservableCollection<T>(this.lifetime)

  val handler: (Unit) -> Unit = {
    result.clear()
    this.forEach { item -> if (f(item)) result.add { item } }
  }

  this.changed.subscribe(this.lifetime, handler)

  handler(Unit)

  return result
}