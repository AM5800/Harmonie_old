package am5800.harmonie.model

import java.io.InputStream
import java.io.OutputStream


public interface FileEnvironment {
    public fun readAsset<T>(path: String, func: (InputStream) -> T): T?
    public fun enumerateAssets(basePath: String): List<String>
    public fun tryReadDataFile<T>(path: String, func: (InputStream) -> T?) : T?
    public fun writeDataFile(path: String, func: (OutputStream) -> Unit)
    public fun appendDataFile(path: String, func: (OutputStream) -> Unit)
}