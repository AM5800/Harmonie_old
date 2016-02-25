package am5800.harmonie.model

import java.util.*


class TextProgress(private val env: FileEnvironment) {
  private val fileName = "textProgress.dat"
  private val textToPartIndex = LinkedHashMap<String, Int>()

  init {
    env.tryReadDataFile(fileName, { s ->
      val reader = InputStreamWrapper(s)
      val n = reader.readInt()
      repeat(n) {
        val id = reader.readString().trim()
        val index = reader.readInt()
        textToPartIndex[id] = index
      }
    })
  }

  fun saveProgress(text: Text, partIndex: Int) {
    if (textToPartIndex[text.id] == partIndex) return
    textToPartIndex[text.id] = partIndex
    save()
  }

  private fun save() {
    env.writeDataFile(fileName, { s ->
      val writer = OutputStreamWrapper(s)
      writer.writeInt(textToPartIndex.size)
      for (entry in textToPartIndex) {
        writer.writeString(entry.key)
        writer.writeInt(entry.value)
      }
    })
  }

  fun loadProgress(text: Text): Int {
    return textToPartIndex[text.id] ?: 0
  }
}