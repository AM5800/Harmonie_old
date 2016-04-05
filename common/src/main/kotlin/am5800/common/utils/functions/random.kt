package am5800.common.utils.functions

import java.util.*

fun <T> List<T>.random(seed: Long?): T {
  if (this.isEmpty()) throw Exception("Collection is empty")
  val random = if (seed != null) Random(seed) else Random()
  val i = random.nextInt(this.size)

  return this[i]
}


