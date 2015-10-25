package am5800.harmonie

import android.content.Context
import java.io.FileNotFoundException
import java.io.InputStream

fun Context.tryReadFile<T>(name: String, func: (InputStream) -> T): T {
    try {
        return this.openFileInput(name).use(func)
    } catch (e: FileNotFoundException) {
        return null
    }
}