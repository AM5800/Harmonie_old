package utilityFunctions

import java.util.*

fun <T> List<T>.shuffle(): List<T> {
  val random = Random()
  val list = this.toMutableList()
  for (i in (list.count() - 1) downTo 1) {
    val j = random.nextInt(i + 1)
    val tmp = list[i]
    list[i] = list[j]
    list[j] = tmp
  }
  return list
}


