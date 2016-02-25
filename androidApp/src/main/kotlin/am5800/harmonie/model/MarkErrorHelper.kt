package am5800.harmonie.model

import am5800.harmonie.model.logging.LoggerProvider
import android.database.sqlite.SQLiteDatabase


class MarkErrorHelper(private val db: SQLiteDatabase, loggerProvider: LoggerProvider) {

  val logger = loggerProvider.getLogger(javaClass)

  init {
    //db.execSQL("DELETE FROM errors")
    val rawQuery = db.rawQuery("SELECT * FROM errors", null)
    while (rawQuery.moveToNext()) {
      val error = rawQuery.getString(0)
      logger.info("MARKED ERROR: " + error)
    }
  }

  fun markError(example: RenderedExample) {
    db.execSQL("INSERT INTO errors (error) VALUES(?)", arrayOf(example.entityId.toString() + " | " + example.text + " | " + example.meanings))
  }
}