package sql

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.services.ContentDb
import am5800.harmonie.app.model.services.Cursor
import java.io.File
import java.sql.DriverManager
import java.sql.ResultSet

class TestCursor(private val rs: ResultSet) : Cursor {
  override fun close() {
    rs.close()
  }

  override fun getString(index: Int): String {
    // Android counts columns from 0, this sql db from 1
    return rs.getString(index + 1)
  }

  override fun moveToNext(): Boolean {
    return rs.next()
  }
}

class TestSqlDatabase(lifetime: Lifetime, dbPath: File) : ContentDb {
  private val connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.absolutePath}");
  private val stmt = connection.createStatement()

  init {
    lifetime.addAction { connection.close() }
  }

  override fun query(query: String): Cursor {
    return TestCursor(stmt.executeQuery(query))
  }

  override fun execute(query: String, vararg args: Any) {
    throw UnsupportedOperationException()
  }

}
