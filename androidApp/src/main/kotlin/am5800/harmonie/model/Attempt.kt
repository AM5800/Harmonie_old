package am5800.harmonie.model

import org.joda.time.DateTime

data class Attempt(val entity: EntityId, val date: DateTime, val score: Float, val note: String?, val success: Boolean)