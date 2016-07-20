package am5800.common

open class Sentence(val uid: String?, val language: Language, val text: String) {
  override fun equals(other: Any?): Boolean{
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as Sentence

    if (uid != other.uid) return false
    if (language != other.language) return false
    if (text != other.text) return false

    return true
  }

  override fun hashCode(): Int{
    var result = uid?.hashCode() ?: 0
    result = 31 * result + language.hashCode()
    result = 31 * result + text.hashCode()
    return result
  }
}