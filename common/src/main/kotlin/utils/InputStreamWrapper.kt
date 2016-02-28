package utils


import java.io.Closeable
import java.io.DataInputStream
import java.io.InputStream

class InputStreamWrapper(stream: InputStream, private val reverseBytes: Boolean = false) : Closeable {
  private val stream = DataInputStream(stream)

  override fun close() {
    stream.close()
  }

  fun readInt(): Int {
    val result = stream.readInt()
    if (reverseBytes) return Integer.reverseBytes(result)
    return result
  }

  fun readString(): String {
    val len = readByte().toInt()
    if (len == 0) return ""

    val result = ByteArray(len)
    stream.readFully(result)
    val resultingString = java.lang.String(result, "UTF-8")
    return resultingString.toString()
  }

  fun readLong(): Long {
    return stream.readLong()
  }

  fun readBool(): Boolean {
    return stream.readBoolean()
  }

  fun readFloat(): Float {
    return stream.readFloat()
  }

  fun readByte(): Byte {
    return stream.readByte()
  }

  fun available(): Int {
    return stream.available()
  }
}