package am5800.harmonie.app.model

import java.util.*


class DebugOptions(val resetProgressOnLaunch: Boolean, val dropPreferredLanguagesOnStart: Boolean, val randomSeed: Long?)

fun DebugOptions.getRandom() = if (this.randomSeed == null) Random() else Random(this.randomSeed)