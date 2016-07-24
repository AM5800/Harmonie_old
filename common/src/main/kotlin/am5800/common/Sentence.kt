package am5800.common

open class Sentence(val uid: String, val language: Language, val text: String, val difficultyLevel: Int?) {
  override fun equals(other: Any?): Boolean{
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as Sentence

    if (uid != other.uid) return false
    if (language != other.language) return false
    if (text != other.text) return false
    if (difficultyLevel != other.difficultyLevel) return false

    return true
  }

  override fun hashCode(): Int{
    var result = uid.hashCode()
    result = 31 * result + language.hashCode()
    result = 31 * result + text.hashCode()
    result = 31 * result + (difficultyLevel ?: 0)
    return result
  }
}