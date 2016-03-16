package am5800.harmonie.ios.model.dbAccess

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.dbAccess.sql.Cursor
import am5800.harmonie.app.model.dbAccess.sql.PermanentDb


class IosPermanentDb(lifetime: Lifetime) : PermanentDb {

  private val instance = DbInstance()

  init {
    lifetime.addAction { instance.close() }
  }

  override fun execute(query: String) {
    instance.executeSQL(query)
  }

  override fun query(query: String): Cursor = IosCursor(instance.createStatement(query))

  private class DbInstance() : IosDbInstanceBase() {

    init {
      open(DBName)
    }

    companion object {
      val DBName = "settings.db"
    }
  }
}