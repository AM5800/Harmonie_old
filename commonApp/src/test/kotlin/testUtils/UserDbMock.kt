package testUtils

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.services.KeyValueDatabase


class KeyValueDatabaseMock : KeyValueDatabase {
  private val properties = mutableMapOf<String, Property<String>>()

  override fun tryGetValue(key: String): String? {
    throw UnsupportedOperationException()
  }

  override fun getValue(key: String, defaultValue: String): String {
    throw UnsupportedOperationException()
  }

  override fun setValue(key: String, value: String) {
    throw UnsupportedOperationException()
  }

  override fun remove(key: String) {
    throw UnsupportedOperationException()
  }

  fun addPropertyForKey(key: String, property: Property<String>) {
    properties[key] = property
  }

  override fun createProperty(lifetime: Lifetime, key: String, defaultValue: String): Property<String> {
    return properties[key]!!
  }
}