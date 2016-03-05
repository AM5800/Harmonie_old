package am5800.harmonie.android.model.dbAccess

import android.database.sqlite.SQLiteDatabase

interface PermanentDbConsumer {
  fun onDbCreate(db: SQLiteDatabase)
  fun onInitialized(db: PermanentDb)
}