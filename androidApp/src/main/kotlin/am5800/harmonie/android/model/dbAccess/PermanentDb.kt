package am5800.harmonie.android.model.dbAccess

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class PermanentDb(context: Context, private val consumers: List<PermanentDbConsumer>) : SqlDatabase {
  override fun execute(query: String) {
    instance.writableDatabase.execSQL(query)
  }

  private val instance = DbInstance(context, consumers)

  override fun query(query: String): Cursor {
    return instance.readableDatabase.rawQuery(query, emptyArray())
  }

  init {
    for (consumer in consumers)
      consumer.onInitialized(this)
  }

  private class DbInstance(context: Context, private val consumers: List<PermanentDbConsumer>) : SQLiteOpenHelper(context, "Settings.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
      for (consumer in consumers) consumer.onDbCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
      throw UnsupportedOperationException()
    }
  }
}