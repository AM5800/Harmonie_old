package testUtils

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.sql.Cursor
import am5800.harmonie.app.model.sql.UserDb
import java.io.File
import java.nio.file.Files
import java.sql.DriverManager

class TestUserSqlDatabase(lifetime: Lifetime) : UserDb {
  private fun createDbFile(lifetime: Lifetime): File {
    val tempFile = Files.createTempFile("harmonie", "db").toFile()
    lifetime.addAction { tempFile.delete() }
    return tempFile
  }

  private val connection = DriverManager.getConnection("jdbc:sqlite:${createDbFile(lifetime).absolutePath}")
  private val stmt = connection.createStatement()

  init {
    lifetime.addAction { connection.close() }
  }

  override fun query(query: String): Cursor {
    return TestCursor(stmt.executeQuery(query))
  }

  override fun execute(query: String, vararg args: Any) {
    stmt.execute(query)
  }
}