package am5800.harmonie.android.dbAccess

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.services.ContentDb
import am5800.harmonie.app.model.services.Cursor
import am5800.harmonie.app.model.services.logging.Logger
import am5800.harmonie.app.model.services.logging.LoggerProvider
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import java.io.File
import java.io.FileOutputStream

class AndroidContentDb(private val context: Context,
                       private val keyValueDb: KeyValueDatabaseImpl,
                       loggerProvider: LoggerProvider,
                       lifetime: Lifetime) : ContentDb {

  override fun execute(query: String) {
    throw UnsupportedOperationException()
  }

  private val logger = loggerProvider.getLogger(javaClass)

  private val db = DbInstance(context, keyValueDb, logger)

  private class DbInstance(private val context: Context,
                           private val keyValueDb: KeyValueDatabaseImpl,
                           private val logger: Logger) : SQLiteOpenHelper(context, DbName, null, 1) {

    init {
      if (checkDbUpdateNeeded()) {
        logger.info("Performing db update")
        close()
        context.assets.open(DbName).use { inStream ->
          FileOutputStream(DbLocation).use { outStream ->
            inStream.copyTo(outStream)
          }
        }
        writableDatabase.close()
      }
    }

    private fun checkDbUpdateNeeded(): Boolean {
      if (!File(DbLocation).exists()) return true
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

  init {
    lifetime.addAction { db.close() }
  }

  companion object {
    private val DbName = "content.db"
    private val DbLocation: String = "/data/data/am5800.harmonie/databases/" + DbName
  }

  override fun query(query: String): Cursor {
    return AndroidCursor(db.readableDatabase.rawQuery(query, emptyArray()))
  }
}