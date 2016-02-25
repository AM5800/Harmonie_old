package am5800.harmonie

import am5800.harmonie.model.Lifetime
import am5800.harmonie.model.logging.LoggerProvider
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import java.io.FileOutputStream

class HarmonieDb(private val context: Context, lifetime: Lifetime, private val settingsDb: SettingsDb, loggerProvider: LoggerProvider) : SQLiteOpenHelper(context, "Harmonie.db", null, 1) {
  companion object {
    private val DbLocation: String = "/data/data/am5800.harmonie/databases/Harmonie.db"
  }

  private val logger = loggerProvider.getLogger(javaClass)

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
  }

  override fun onCreate(db: SQLiteDatabase) {
  }

  val dbIsUpdatedThisLaunch: Boolean

  init {
    if (checkDbUpdateNeeded()) {
      logger.info("Performing db update")
      close()
      context.assets.open("Harmonie.db").use { inStream ->
        FileOutputStream(DbLocation).use { outStream ->
          inStream.copyTo(outStream)
        }
      }
      writableDatabase.close()
      dbIsUpdatedThisLaunch = true
    } else dbIsUpdatedThisLaunch = false
  }

  private fun checkDbUpdateNeeded(): Boolean {
    val dbKey = "HarmonieDbHash"
    val previousHash = settingsDb.getValue(dbKey, "0")

    val dbStream = context.assets.open("Harmonie.db")
    val bytes = ByteStreams.toByteArray(dbStream)
    val hash = Hashing.md5().hashBytes(bytes).toString()
    settingsDb.setValue(dbKey, hash)

    return hash != previousHash
  }

  fun rawQuery(sql: String, selectionArgs: Array<String>): Cursor {
    return readableDatabase.rawQuery(sql, selectionArgs)
  }


}


