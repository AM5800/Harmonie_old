package am5800.common.utils

import java.io.DataOutputStream
import java.io.OutputStream

class OutputStreamWrapper(stream: OutputStream, private val reverseBytes: Boolean = false) : java.io.Closeable {
  private val stream = DataOutputStream(stream)

  override fun close() {
    stream.close()
  }

  fun writeInt(int: Int) {
    if (reverseBytes) stream.writeInt(Integer.reverseBytes(int))
    else stream.writeInt(int)
  }

  fun writeString(str: String?) {
    if (str == null) {
      writeByte(0)
      return
    }

    val bytes = str.toByteArray(Charsets.UTF_8)
    writeByte(bytes.count())
    stream.write(bytes)
  }

  fun writeLong(long: Long) {
    stream.writeLong(long)
  }

  fun writeBool(bool: Boolean) {
    stream.writeBoolean(bool)
  }

  fun writeFloat(float: Float) {
    stream.writeFloat(float)
  }

  fun writeByte(byte: Int) {
    stream.writeByte(byte)
  }
}