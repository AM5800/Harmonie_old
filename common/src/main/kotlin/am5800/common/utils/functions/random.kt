package am5800.common.utils.functions

import java.util.*

fun <T> List<T>.random(random: Random? = null): T {
  if (this.isEmpty()) throw Exception("Collection is empty")
  val rnd = random ?: Random()
  val i = rnd.nextInt(this.size)

  return this[i]
}


