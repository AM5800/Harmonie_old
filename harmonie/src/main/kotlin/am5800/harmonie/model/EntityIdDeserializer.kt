package am5800.harmonie.model


interface EntityIdDeserializer {
  fun tryDeserialize(string: String): EntityId?
}

fun List<EntityIdDeserializer>.deserialize(string: String): EntityId {
  for (deserializer in this) {
    val id = deserializer.tryDeserialize(string)
    if (id != null) return id
  }

  throw Exception("Can't deserialize entity: " + string)
}