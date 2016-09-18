package am5800.harmonie.app.model

import java.util.*


class DebugOptions(val dropAttemptsOnStart: Boolean, randomSeed: Long?, val dropErrorReportsOnStart: Boolean) {
  val random = if (randomSeed == null) Random() else Random(randomSeed)
}