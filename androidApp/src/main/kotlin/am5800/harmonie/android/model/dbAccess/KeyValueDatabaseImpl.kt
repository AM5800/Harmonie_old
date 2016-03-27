package am5800.harmonie.android.model.dbAccess

import am5800.harmonie.app.model.dbAccess.KeyValueDatabase
import am5800.harmonie.app.model.dbAccess.sql.query1

class KeyValueDatabaseImpl(private val db: AndroidPermanentDb) : KeyValueDatabase {
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