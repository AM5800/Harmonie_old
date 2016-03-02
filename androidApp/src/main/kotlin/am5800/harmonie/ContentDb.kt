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

inline fun <reified T> valueFromCursor(index: Int, cursor: Cursor): T {
  if ("" is T) {
    return cursor.getString(index) as T
  }
  if (0 is T) {
    return cursor.getInt(index) as T
  }
  if (0.0 is T) {
    return cursor.getDouble(index) as T
  }
  if (0L is T) {
    return cursor.getLong(index) as T
  }

  throw Exception("Unsupported type: ${T::class.simpleName}")
}

data class Tuple4<T1, T2, T3, T4>(val value1: T1, val value2: T2, val value3: T3, val value4: T4)
data class Tuple3<T1, T2, T3>(val value1: T1, val value2: T2, val value3: T3)

inline fun <reified T1, reified T2> ContentDb.query2(query: String): List<Pair<T1, T2>> {
  val cursor = this.rawQuery(query, emptyArray())
  val result = mutableListOf<Pair<T1, T2>>()
  while (cursor.moveToNext()) {
    val value1 = valueFromCursor<T1>(0, cursor)
    val value2 = valueFromCursor<T2>(1, cursor)
    result.add(Pair(value1, value2))
  }

  return result
}

inline fun <reified T1, reified T2, reified T3, reified T4> ContentDb.query4(query: String): List<Tuple4<T1, T2, T3, T4>> {
  val cursor = this.rawQuery(query, emptyArray())
  val result = mutableListOf<Tuple4<T1, T2, T3, T4>>()
  while (cursor.moveToNext()) {
    val value1 = valueFromCursor<T1>(0, cursor)
    val value2 = valueFromCursor<T2>(1, cursor)
    val value3 = valueFromCursor<T3>(2, cursor)
    val value4 = valueFromCursor<T4>(3, cursor)
    result.add(Tuple4(value1, value2, value3, value4))
  }

  return result
}

inline fun <reified T1, reified T2, reified T3> ContentDb.query3(query: String): List<Tuple3<T1, T2, T3>> {
  val cursor = this.rawQuery(query, emptyArray())
  val result = mutableListOf<Tuple3<T1, T2, T3>>()
  while (cursor.moveToNext()) {
    val value1 = valueFromCursor<T1>(0, cursor)
    val value2 = valueFromCursor<T2>(1, cursor)
    val value3 = valueFromCursor<T3>(2, cursor)
    result.add(Tuple3(value1, value2, value3))
  }

  return result
}

