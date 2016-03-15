package am5800.harmonie.ios.model.dbAccess

import am5800.harmonie.app.model.dbAccess.sql.Cursor
import sqlite.c.Globals


class IosCursor(private var stmt: IosSQLiteStatement) : Cursor {
  var isAfterLast: Boolean = moveToNext()
    private set

  override fun moveToNext(): Boolean {
    isAfterLast = stmt.step()
    return isAfterLast
  }

  override fun close() {
    stmt.close()
  }

  // TODO: delegate to statement to hide internals
  override fun getString(columnIndex: Int): String = stmt.let { Globals.sqlite3_column_text(it.stmtHandle, columnIndex) }
}