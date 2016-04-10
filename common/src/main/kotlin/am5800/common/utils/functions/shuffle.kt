package am5800.common.utils.functions

import java.util.*

fun <T> List<T>.shuffle(random: Random? = null): List<T> {
  val rnd = random ?: Random()
  val list = this.toMutableList()
  for (i in (list.count() - 1) downTo 1) {
    val j = rnd.nextInt(i + 1)
    val tmp = list[i]
    list[i] = list[j]
    list[j] = tmp
  }
  return list
}


