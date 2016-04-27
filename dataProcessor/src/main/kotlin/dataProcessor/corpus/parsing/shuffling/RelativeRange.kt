package dataProcessor.corpus.parsing.shuffling

class RelativeRange(val start: Int, val end: Int) {
  init {
    if (start < 0) throw IllegalArgumentException("start can't be less than zero")
    if (end > 100) throw IllegalArgumentException("end can't be more than hundred")
    if (start > end) throw IllegalArgumentException("start can't be more than end")
  }

  fun accept(i: Int, n: Int): Boolean {
    val value = 100 * i / n
    return value >= start && value < end
  }
}