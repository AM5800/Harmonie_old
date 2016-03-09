package am5800.harmonie.android.model.dbAccess

import am5800.harmonie.app.model.dbAccess.KeyValueDatabase
import android.database.sqlite.SQLiteDatabase

class KeyValueDatabaseImpl() : PermanentDbConsumer, KeyValueDatabase {
  var db: PermanentDb? = null
  override fun onDbCreate(db: SQLiteDatabase) {
    db.execSQL("CREATE TABLE simpleSettings (key TEXT PRIMARY KEY, value TEXT)")
  }

  override fun onInitialized(db: PermanentDb) {
    this.db = db
  }

  override fun getValue(key: String, defaultValue: String): String {
    return db!!.query1<String>("SELECT value FROM simpleSettings WHERE key = '$key'").singleOrNull() ?: defaultValue
  }

  override fun setValue(key: String, value: String) {
    db!!.execute("INSERT OR REPLACE INTO simpleSettings(key, value) VALUES ('$key', '$value')")
  }
}