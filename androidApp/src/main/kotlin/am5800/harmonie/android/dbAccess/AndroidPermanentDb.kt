package am5800.harmonie.android.dbAccess

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.services.Cursor
import am5800.harmonie.app.model.services.PermanentDb
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File


class AndroidPermanentDb(context: Context, lifetime: Lifetime) : PermanentDb {
  override fun execute(query: String) {
    instance.writableDatabase.execSQL(query)
  }

  private val instance = DbInstance(context)

  init {
    lifetime.addAction { instance.close() }
  }

  override fun query(query: String): Cursor {
    return AndroidCursor(instance.readableDatabase.rawQuery(query, emptyArray()))
  }

  private class DbInstance(context: Context) : SQLiteOpenHelper(context, DbName, null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
      throw UnsupportedOperationException()
    }
  }

  companion object {
    private val DbName = "permanent.db"
    private val DbLocation: String = "/data/data/am5800.harmonie/databases/" + DbName
  }

  fun getLocation(): File {
    return File(DbLocation)
  }
}