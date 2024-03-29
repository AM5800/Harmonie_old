package am5800.harmonie.android.dbAccess

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.sql.KeyValueDatabase
import am5800.harmonie.app.model.sql.query1

class KeyValueDatabaseImpl(private val db: AndroidUserDb) : KeyValueDatabase {
  override fun createProperty(lifetime: Lifetime, key: String, defaultValue: String): Property<String> {
    val result = Property(lifetime, getValue(key, defaultValue))
    result.onChange(lifetime, {
      if (it.isAcknowledge) return@onChange
      val newValue = it.newValue
      setValue(key, newValue)
    })
    return result
  }

  override fun remove(key: String) {
    db.execute("DELETE FROM simpleSettings WHERE key='$key'")
  }

  override fun tryGetValue(key: String): String? {
    return db.query1<String>("SELECT value FROM simpleSettings WHERE key = '$key'").singleOrNull()
  }

  init {
    db.execute("CREATE TABLE IF NOT EXISTS simpleSettings (key TEXT PRIMARY KEY, value TEXT)")
  }

  override fun getValue(key: String, defaultValue: String): String {
    return tryGetValue(key) ?: defaultValue
  }

  override fun setValue(key: String, value: String) {
    db.execute("INSERT OR REPLACE INTO simpleSettings(key, value) VALUES ('$key', '$value')")
  }
}