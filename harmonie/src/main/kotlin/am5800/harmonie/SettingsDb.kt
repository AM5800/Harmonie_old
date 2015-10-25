package am5800.harmonie

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


public class SettingsDb(context : Context)  : SQLiteOpenHelper(context, "Settings.db", null, 1){
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ERRORS (error TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        throw UnsupportedOperationException()
    }
}