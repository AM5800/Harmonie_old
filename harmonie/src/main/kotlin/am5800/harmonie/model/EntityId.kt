package am5800.harmonie.model

public interface EntityId {
    public fun serialize() : String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}