package am5800.harmonie.model

import java.io.InputStream
import java.io.OutputStream


public class LineReaderWriter {
    companion object {
        public fun read<T>(stream: InputStream, f: (String) -> T): List<T> {
            val lines = stream.readFileAsString().splitAtLines().filter { it.isNotEmpty() }
            return lines.map(f).toList()
        }

        public fun write(lines: List<String>, stream: OutputStream) {
            val builder = StringBuilder()
            for (line in lines) builder.appendln(line)
            stream.write(builder.toString().toByteArray(Charsets.UTF_8))
        }
    }
}