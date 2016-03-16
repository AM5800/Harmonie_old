package am5800.harmonie.ios.model.dbAccess

import com.intel.inde.moe.natj.general.ptr.BytePtr
import com.intel.inde.moe.natj.general.ptr.Ptr
import com.intel.inde.moe.natj.general.ptr.VoidPtr
import com.intel.inde.moe.natj.general.ptr.impl.PtrFactory
import ios.foundation.NSFileManager
import ios.foundation.NSURL
import ios.foundation.enums.NSSearchPathDirectory
import ios.foundation.enums.NSSearchPathDomainMask
import sqlite.c.Globals
import java.io.File
import java.io.IOException

open class IosDbInstanceBase {
  protected val applicationDocumentsDirectory: String
    get() = (NSFileManager.defaultManager().URLsForDirectoryInDomains(NSSearchPathDirectory.DocumentDirectory, NSSearchPathDomainMask.UserDomainMask).firstObject() as NSURL).fileSystemRepresentation()

  protected fun resourceAsStream(id: String) = IosDbInstanceBase::class.java.getResourceAsStream("/" + id) ?: throw Exception("Unable to find resource $id")

  private var connectionHandle: VoidPtr? = null
  private val connectionLock = 0

  protected fun open(dbname: String): Unit {
    synchronized(connectionLock) {
      val dbFile = File(applicationDocumentsDirectory, dbname)
      val dbHandleRef = PtrFactory.newPointerPtr(Void::class.java, 2, 1, true, false) as Ptr<VoidPtr>
      val res = Globals.sqlite3_open(dbFile.canonicalPath, dbHandleRef)
      if (res != 0) throw IOException("Failed to open/create database file, result code: $res")
      connectionHandle = dbHandleRef.get()
    }
  }

  internal fun close() {
    synchronized(connectionLock) {
      if (connectionHandle != null) {
        Globals.sqlite3_close(connectionHandle)
        connectionHandle = null
      }
    }
  }

  fun createStatement(query: String) = IosSQLiteStatement(connectionHandle!!, query)

  fun executeSQL(query: String): Unit {
    val errMsg = PtrFactory.newPointerPtr(Void::class.java, 2, 1, true, false) as Ptr<BytePtr>
    val res = Globals.sqlite3_exec(connectionHandle!!, query, null, null, errMsg)
    if (res != 0)
      throw RuntimeException(Globals.sqlite3_errmsg(connectionHandle))
  }
}

