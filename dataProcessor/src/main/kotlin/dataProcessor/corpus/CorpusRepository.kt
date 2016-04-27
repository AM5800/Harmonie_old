package dataProcessor.corpus

import java.io.*
import java.util.*

class CorpusRepository(basePath: File) {
  private val corpuses = parseInfos(basePath)

  private fun parseInfos(basePath: File): List<CorpusInfo> {
    val fileList = basePath.listFiles { f -> f.extension.equals("cdef", true) } ?: return emptyList()

    return fileList.map { file -> tryParseCorpusInfo(file.absoluteFile) }.filterNotNull()
  }

  private fun tryParseCorpusInfo(infoFile: File): CorpusInfo? {
    try {
      BufferedReader(FileReader(infoFile)).use { reader ->
        val format = reader.readLine()!!.trim()
        val metadata = LinkedHashMap<String, String>()

        while (true) {
          val line = reader.readLine() ?: break

          val splitted = line.split("=").map { it.trim() }.toList()
          if (splitted.size != 2) continue

          metadata.put(splitted[0], splitted[1])
        }

        return CorpusInfo(infoFile, format, metadata)
      }

    } catch (e: Exception) {
      // TODO need logging!
      val sw = StringWriter()
      e.printStackTrace(PrintWriter(sw))
      println(sw.toString())
      return null
    }
  }

  fun getCorpuses(): List<CorpusInfo> {
    return corpuses
  }
}