package am5800.harmonie.android

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class PermanentDb(context: Context) : SQLiteOpenHelper(context, "Settings.db", null, 1) {
  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL("CREATE TABLE errors (error TEXT)")
    db.execSQL("CREATE TABLE simpleSettings (key TEXT PRIMARY KEY, value TEXT)")
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    throw UnsupportedOperationException()
  }

  fun getValue(key: String, defaultValue: String): String {
    val cursor = this.readableDatabase.rawQuery("SELECT value FROM simpleSettings WHERE key = ?", arrayOf(key))
    var result = defaultValue
    while (cursor.moveToNext()) {
      result = cursor.getString(0)
    }
    return result
  }

  fun setValue(key: String, value: String) {
    this.writableDatabase.execSQL("INSERT OR REPLACE INTO simpleSettings(key, value) VALUES (?, ?)", arrayOf(key, value))
  }
}