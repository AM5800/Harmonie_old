package testUtils

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

class TestSqlDatabase(lifetime: Lifetime) : ContentDb {
  private val connection = DriverManager.getConnection("jdbc:sqlite:${findDb().absolutePath}");
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

  private fun findDb(): File {
    val locations = listOf(File("data\\test.db"), File("..\\data\\test.db"))
    val selectedLocation = locations.firstOrNull { it.exists() }
    if (selectedLocation != null) return selectedLocation

    throw Exception("Database not found. Tried locations: " + locations.joinToString(", ") { it.absolutePath })
  }

}
