package am5800.harmonie.model

import utilityFunctions.clamp

data class RenderedExample(val entityId: EntityId, val text: String, val meanings: List<String>) {
  override fun toString(): String {
    return "$text -> ${meanings.joinToString(" | ")}"
  }
}

open class ExamplesRenderer() {
  fun render(example: Example): RenderedExample {
    val ranges = example.ranges.sortedBy { it.start }
    assertNotOverlapping(ranges, example.entityId)

    val sb = StringBuilder(example.text)
    for (range in ranges.reversed()) {
      val actual = sb.substring(range.start, range.start + range.length)
      sb.replace(range.start, range.start + range.length, "<b>$actual</b>")
    }

    val start = ranges.first().start - 50
    val end = ranges.last().start + ranges.last().length + 50
    val text = sb.toString().substring(start.clamp(0, sb.length), end.clamp(0, sb.length))

    return RenderedExample(example.entityId, text, example.meanings)
  }

  private fun assertNotOverlapping(sortedRanges: List<ExampleRange>, entityId: EntityId) {
    var lastEnd = -1
    for (range in sortedRanges) {
      if (range.start < lastEnd) throw Exception("Overlapping ranges detected: " + entityId)
      lastEnd = range.start + range.length
    }
  }
}


