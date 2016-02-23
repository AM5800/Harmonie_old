package am5800.harmonie.model

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

fun InputStream.readFileAsString(): String {
    val buffer: CharArray = CharArray(16384)
    val sb = StringBuilder()
    val reader = InputStreamReader(this, "UTF-8")

    var n = 1
    while (true) {
        n = reader.read(buffer)
        if (n < 0) break
        sb.append(buffer, 0, n)
    }

    return sb.toString()
}

fun File.readFileAsString(): String {
    return FileInputStream(this).use { s -> s.readFileAsString () }
}

fun String.splitAtLines(): Array<String> {
    return this.split("\\r?\\n".toRegex()).toTypedArray()
}

fun <T> List<T>.shuffle(): List<T> {
    val random = Random()
    val list = this.toMutableList()
    for (i in (list.count() - 1) downTo 1) {
        val j = random.nextInt(i + 1)
        val tmp = list[i]
        list[i] = list[j]
        list[j] = tmp
    }
    return list
}

fun Int.clamp(lowerBound: Int, upperBound: Int): Int {
    if (this > upperBound) return upperBound
    if (this < lowerBound) return lowerBound
    return this
}

