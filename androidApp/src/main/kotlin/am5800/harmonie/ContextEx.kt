package am5800.harmonie

import android.content.Context
import java.io.FileNotFoundException
import java.io.InputStream

fun <T> Context.tryReadFile(name: String, func: (InputStream) -> T): T? {
  try {
    return this.openFileInput(name).use(func)
  } catch (e: FileNotFoundException) {
    return null
  }
}