package am5800.harmonie.ios.model.dbAccess

import com.intel.inde.moe.natj.general.ptr.Ptr
import com.intel.inde.moe.natj.general.ptr.VoidPtr
import com.intel.inde.moe.natj.general.ptr.impl.PtrFactory
import sqlite.c.Globals

class IosSQLiteStatement(internal val dbHandle: VoidPtr, val statement: String?, private val bindArgs: Array<Any?> = emptyArray()) {

  internal var stmtHandle: VoidPtr? = null
    private set

  var affectedCount = 0
    private set

  var lastInsertedID: Long = -1
    private set

  init {
    @SuppressWarnings("unchecked")
    val stmtRef = PtrFactory.newPointerPtr(Void::class.java, 2, 1, true, false) as Ptr<VoidPtr>
    if (Globals.sqlite3_prepare_v2(dbHandle, statement, -1, stmtRef, null) != 0)
      throw RuntimeException(Globals.sqlite3_errmsg(dbHandle))

    stmtHandle = stmtRef.get()
    bindArgs.forEachIndexed { idx, bind ->
      if (when (bind) {
        is String -> Globals.sqlite3_bind_text(stmtHandle, idx + 1, bind as String?, -1, { })
        is Int -> Globals.sqlite3_bind_int(stmtHandle, idx + 1, bind)
        is Long -> Globals.sqlite3_bind_int64(stmtHandle, idx + 1, bind)
        is Double -> Globals.sqlite3_bind_double(stmtHandle, idx + 1, bind)
        null -> Globals.sqlite3_bind_null(stmtHandle, idx + 1)
        else -> throw RuntimeException("No implemented SQLite3 bind function found for " + bind.javaClass.name)
      } != 0) {
        throw RuntimeException(Globals.sqlite3_errmsg(dbHandle))
      }
    }
  }

  internal fun close() {
    if (stmtHandle != null) {
      Globals.sqlite3_finalize(stmtHandle)
      stmtHandle = null
    }
  }

  internal fun step(): Boolean {
    if (stmtHandle == null) throw RuntimeException("statement handle is closed")
    val res = Globals.sqlite3_step(stmtHandle)
    return when (res) {
      100 -> true // SQLITE_ROW
      101 -> false // SQLITE_DONE
      else -> throw RuntimeException("code: $res, msg: ${Globals.sqlite3_errmsg(dbHandle)}")
    }
  }

  internal fun reset() {
    if (stmtHandle == null) throw RuntimeException("statement handle is closed")
    Globals.sqlite3_reset(stmtHandle)
  }
}