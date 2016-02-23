package am5800.harmonie.model

interface EntityId {
  fun serialize(): String
  override fun equals(other: Any?): Boolean
  override fun hashCode(): Int
  override fun toString(): String
}