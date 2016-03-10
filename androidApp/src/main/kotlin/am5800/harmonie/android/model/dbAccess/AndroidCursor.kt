package am5800.harmonie.android.model.dbAccess

import am5800.harmonie.app.model.dbAccess.sql.Cursor


class AndroidCursor(private val cursor: android.database.Cursor) : Cursor {
  override fun getString(index: Int): String {
    return cursor.getString(index)
  }

  override fun moveToNext(): Boolean {
    return cursor.moveToNext()
  }
}