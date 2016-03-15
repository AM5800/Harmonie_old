package am5800.harmonie.ios.model.dbAccess

import am5800.harmonie.app.model.dbAccess.sql.ContentDb
import am5800.harmonie.app.model.dbAccess.sql.ContentDbConsumer
import am5800.harmonie.app.model.dbAccess.sql.Cursor
import am5800.harmonie.app.model.logging.Logger
import am5800.harmonie.app.model.logging.LoggerProvider
import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class IosContentDb(private val keyValueDb: KeyValueDatabaseImpl,
                   loggerProvider: LoggerProvider,
                   dbConsumers: List<ContentDbConsumer>) : ContentDb {
  override fun execute(query: String) {
    throw UnsupportedOperationException()
  }

  private val logger = loggerProvider.getLogger(javaClass)

  private val db = DbInstance(keyValueDb, logger, dbConsumers, this)

  private class DbInstance(private val keyValueDb: KeyValueDatabaseImpl,
                           private val logger: Logger,
                           dbConsumers: List<ContentDbConsumer>,
                           db: IosContentDb) : IosDbInstanceBase() {

    init {
      fun checkDbUpdateNeeded(): Boolean {
        val dbKey = "ContentDbChecksum"
        val previousChecksum = keyValueDb.getValue(dbKey, "0")

        val dbStream = resourceAsStream(DbName)
        val bytes = ByteStreams.toByteArray(dbStream)
        val checksum = Hashing.md5().hashBytes(bytes).toString()
        keyValueDb.setValue(dbKey, checksum)

        return checksum != previousChecksum
      }

      if (checkDbUpdateNeeded()) {
        dbConsumers.forEach { it.dbMigrationPhase1(db) }
        logger.info("Performing db update")
        prepareFromResources(DbName)
        dbConsumers.forEach { it.dbMigrationPhase2(db) }
      }

      dbConsumers.forEach { it.dbInitialized(db) }
      open(DbName)
    }

    private fun prepareFromResources(id: String) {
      val dbFile = File(applicationDocumentsDirectory, id)
      if (dbFile.exists()) throw Exception("Destination file already exists '$dbFile'")

      val dbSource = resourceAsStream(id)

      var os: OutputStream? = null
      try {
        os = FileOutputStream(dbFile)
        val buffer = ByteArray(1024)
        while (true) {
          val length = dbSource.read(buffer)
          if (length <= 0) break;
          os.write(buffer, 0, length)
        }
      } finally {
        dbSource.close()
        os?.close()
      }
    }
  }

  companion object {
    private val DbName = "content.db"
    private val DbLocation: String = "/data/data/am5800.harmonie/databases/" + DbName
  }

  override fun query(query: String): Cursor = IosCursor(db.createStatement(query))
}