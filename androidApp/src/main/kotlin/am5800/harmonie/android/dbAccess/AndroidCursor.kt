package am5800.harmonie.android.dbAccess

import am5800.harmonie.app.model.services.Cursor


class AndroidCursor(private val cursor: android.database.Cursor) : Cursor {
  override fun close() {
    cursor.close()
  }

  override fun getString(index: Int): String {
    return cursor.getString(index)
  }

  override fun moveToNext(): Boolean {
    return cursor.moveToNext()
  }
}