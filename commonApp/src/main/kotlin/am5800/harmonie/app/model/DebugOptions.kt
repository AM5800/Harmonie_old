package am5800.harmonie.app.model

import java.util.*


class DebugOptions(val resetProgressOnLaunch: Boolean, val dropPreferredLanguagesOnStart: Boolean, randomSeed: Long?) {
  val random = if (randomSeed == null) Random() else Random(randomSeed)
}