package am5800.harmonie

import am5800.harmonie.model.logging.Logger
import am5800.harmonie.model.logging.LoggerProvider
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import java.io.FileOutputStream

interface ContentDbConsumer {
  fun dbMigrationPhase1(oldDb: ContentDb)
  fun dbMigrationPhase2(newDb: ContentDb)
  fun dbInitialized(db: ContentDb)
}

class ContentDb(private val context: Context,
                private val permanentDb: PermanentDb,
                loggerProvider: LoggerProvider,
                dbConsumers: List<ContentDbConsumer>) {

  private val logger = loggerProvider.getLogger(javaClass)

  private val db = DbInstance(context, permanentDb, logger, dbConsumers, this)

  private class DbInstance(private val context: Context,
                           private val permanentDb: PermanentDb,
                           private val logger: Logger,
                           dbConsumers: List<ContentDbConsumer>,
                           db: ContentDb) : SQLiteOpenHelper(context, DbName, null, 1) {

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
      val previousChecksum = permanentDb.getValue(dbKey, "0")

      val dbStream = context.assets.open(DbName)
      val bytes = ByteStreams.toByteArray(dbStream)
      val checksum = Hashing.md5().hashBytes(bytes).toString()
      permanentDb.setValue(dbKey, checksum)

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

  fun rawQuery(sql: String, selectionArgs: Array<String>): Cursor {
    return db.readableDatabase.rawQuery(sql, selectionArgs)
  }
}


