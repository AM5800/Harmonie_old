package am5800.harmonie.android.model

import java.io.InputStream
import java.io.OutputStream


interface FileEnvironment {
  fun <T> readAsset(path: String, func: (InputStream) -> T): T?
  fun enumerateAssets(basePath: String): List<String>
  fun <T> tryReadDataFile(path: String, func: (InputStream) -> T?): T?
  fun writeDataFile(path: String, func: (OutputStream) -> Unit)
  fun appendDataFile(path: String, func: (OutputStream) -> Unit)
}