package am5800.harmonie.android.model.dbAccess

import am5800.harmonie.app.model.dbAccess.sql.ContentDb
import am5800.harmonie.app.model.dbAccess.sql.ContentDbConsumer
import am5800.harmonie.app.model.dbAccess.sql.Cursor
import am5800.harmonie.app.model.logging.Logger
import am5800.harmonie.app.model.logging.LoggerProvider
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import java.io.FileOutputStream

class AndroidContentDb(private val context: Context,
                       private val keyValueDb: KeyValueDatabaseImpl,
                       loggerProvider: LoggerProvider,
                       dbConsumers: List<ContentDbConsumer>) : ContentDb {
  override fun execute(query: String) {
    throw UnsupportedOperationException()
  }

  private val logger = loggerProvider.getLogger(javaClass)

  private val db = DbInstance(context, keyValueDb, logger, dbConsumers, this)

  private class DbInstance(private val context: Context,
                           private val keyValueDb: KeyValueDatabaseImpl,
                           private val logger: Logger,
                           dbConsumers: List<ContentDbConsumer>,
                           db: AndroidContentDb) : SQLiteOpenHelper(context, DbName, null, 1) {

    init {
      if (checkDbUpdateNeeded()) {
        dbConsumers.forEach { it.dbMigrationPhase1(db) }
        logger.info("Performing db update")
        close()
        context.assets.open(DbName).use { inStream ->
          FileOutputStream(DbLocation).use { outStream ->
            inStream.copyTo(outStream)
          }
        }
        writableDatabase.close()
        dbConsumers.forEach { it.dbMigrationPhase2(db) }
      }

      dbConsumers.forEach { it.dbInitialized(db) }
    }

    private fun checkDbUpdateNeeded(): Boolean {
      val dbKey = "ContentDbChecksum"
      val previousChecksum = keyValueDb.getValue(dbKey, "0")

      val dbStream = context.assets.open(DbName)
      val bytes = ByteStreams.toByteArray(dbStream)
      val checksum = Hashing.md5().hashBytes(bytes).toString()
      keyValueDb.setValue(dbKey, checksum)

      return checksum != previousChecksum
    }

    override fun onCreate(p0: SQLiteDatabase?) {
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }
  }

  companion object {
    private val DbName = "content.db"
    private val DbLocation: String = "/data/data/am5800.harmonie/databases/" + DbName
  }

  override fun query(query: String): Cursor {
    return AndroidCursor(db.readableDatabase.rawQuery(query, emptyArray()))
  }
}