package am5800.common.utils.functions

import java.util.*

fun <T> List<T>.shuffle(seed: Long?): List<T> {
  val random = if (seed != null) Random(seed) else Random()
  val list = this.toMutableList()
  for (i in (list.count() - 1) downTo 1) {
    val j = random.nextInt(i + 1)
    val tmp = list[i]
    list[i] = list[j]
    list[j] = tmp
  }
  return list
}


