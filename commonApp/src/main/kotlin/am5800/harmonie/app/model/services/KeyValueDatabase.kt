package am5800.harmonie.app.model.services

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property

interface KeyValueDatabase {
  fun tryGetValue(key: String): String?
  fun getValue(key: String, defaultValue: String): String
  fun setValue(key: String, value: String)
  fun remove(key: String)
  fun createProperty(lifetime: Lifetime, key: String, defaultValue: String): Property<String>
}