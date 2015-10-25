package am5800.harmonie.model

import am5800.harmonie.model.words.PartOfSpeech
import com.google.common.base.Objects

public class GermanWordId(public val word: String, public val partOfSpeech: PartOfSpeech, public val gender: Gender?) : EntityId {
    init {
        if (word.length() < 2) throw Exception("Too short word:" + word)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GermanWordId) return false
        if (other === this) return true

        return other.word == word && other.partOfSpeech == partOfSpeech && other.gender == gender
    }

    override fun hashCode(): Int {
        return Objects.hashCode(word, partOfSpeech, gender)
    }

    override fun toString(): String {
        if (partOfSpeech == PartOfSpeech.Noun && gender != null) return presentAsNoun(gender)
        return word
    }

    private fun presentAsNoun(gender: Gender): String {
        val sb = StringBuilder()
        sb.append(when (gender) {
            Gender.Masculine -> "der"
            Gender.Neuter -> "das"
            Gender.Feminine -> "die"
        })
        sb.append(" ")
        sb.append(word.first().toUpperCase())
        for (letter in word.drop(1)) {
            sb.append(letter.toLowerCase())
        }
        return sb.toString()
    }

    override fun serialize(): String {
        return "de:" + word + "|" + partOfSpeech.short() + gender.short()
    }

    public fun PartOfSpeech.short(): String {
        return when (this) {
            PartOfSpeech.Noun -> "n"
            PartOfSpeech.Adjective -> "j"
            PartOfSpeech.Adverb -> "a"
            PartOfSpeech.Verb -> "v"
            else -> ""
        }
    }

    fun Gender?.short(): String {
        return when (this) {
            Gender.Feminine -> "f"
            Gender.Masculine -> "m"
            Gender.Neuter -> "n"
            else -> ""
        }
    }
}