package am5800.harmonie

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

public class HarmonieDb(private val context : Context) : SQLiteOpenHelper(context, "Harmonie.db", null, 1) {
    companion object {
        private val DbLocation : String = "/data/data/am5800.harmonie/databases/Harmonie.db"
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    override fun onCreate(db: SQLiteDatabase) {
    }

    public fun importDatabase() {
        close()
        context.getAssets().open("Harmonie.db").use { inStream ->
            FileOutputStream(DbLocation).use { outStream ->
                inStream.copyTo(outStream)
            }
        }
        getWritableDatabase().close()
    }

}


