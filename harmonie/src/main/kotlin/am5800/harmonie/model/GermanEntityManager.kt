package am5800.harmonie.model

import am5800.harmonie.HarmonieDb
import am5800.harmonie.model.words.PartOfSpeech
import android.database.sqlite.SQLiteDatabase
import java.util.*

public class GermanEntityManager(private val db: HarmonieDb) : EntityManager, EntityIdDeserializer {
    override fun getEntitiesForText(textPartId: TextPartId): List<EntityId> {

        val result = ArrayList<EntityId>()

        val args = arrayOf(textPartId.textId, textPartId.partNumber.toString())
        val wordsCursor = db.rawQuery(
                "SELECT word, partOfSpeech, gender FROM germanWords WHERE id IN (" +
                    "SELECT wordId FROM germanExamples WHERE partID IN (" +
                        "SELECT id FROM textParts WHERE textId = ? AND partNumber = ?" +
                    ")" +
                ")", args)

        while (wordsCursor.moveToNext()) {
            val nf = wordsCursor.getString(0)
            val posString : String = wordsCursor.getString(1)
            val genderString : String? = wordsCursor.getString(2)

            val pos = PartOfSpeech.valueOf(posString)
            val gender = if (genderString == null) null else Gender.valueOf(genderString)
            result.add(GermanWordId(nf, pos, gender))
        }

        return result
    }

    override fun getExamples(entityId: EntityId): List<Example> {
        if (entityId !is GermanWordId) return emptyList()

        val nf = entityId.word
        val pos = entityId.partOfSpeech.toString()
        val gender = entityId.gender

        val result = ArrayList<Example>()

        val args = arrayOf(nf, pos)
        val examplesCursor = db.rawQuery(
                "SELECT meanings,ranges,partText FROM germanExamples " +
                    "INNER JOIN textParts ON partId = textParts.id " +
                "WHERE wordId IN " +
                    "(SELECT id FROM germanWords WHERE word = ? AND partOfSpeech = ? And gender ??)".handleGender(gender), args)

        while (examplesCursor.moveToNext()) {
            val meaningsStr = examplesCursor.getString(0)
            val rangesStr = examplesCursor.getString(1)
            val text = examplesCursor.getString(2)

            val meanings = parseMeanings(meaningsStr)
            val ranges = parseRanges(rangesStr)

            result.add(Example(text, entityId, meanings, ranges))
        }

        return result
    }

    private fun parseRanges(rangesStr: String): List<ExampleRange> {
        val ranges = rangesStr.split("|")
        return ranges.map {
            val list = it.split(",").map {it.trim()}
            if (list.count() != 2) throw Exception("Can't parse range: " + rangesStr)
            val start = Integer.parseInt(list[0])
            val length = Integer.parseInt(list[1])
            ExampleRange(start, length)
        }
    }

    private fun parseMeanings(meaningsStr: String): List<String> {
        return meaningsStr.split("|").map { it.trim() }
    }

    private fun String.handleGender(gender : Gender?) : String {
        if (gender == null) {
            return this.replace("??", "IS NULL")
        }
        else {
            return this.replace("??", "= \"$gender\"")
        }
    }

    override fun tryDeserialize(string: String): EntityId? {
        if (!string.startsWith("de:")) return null;

        val id = string.substring(3)
        val parts = id.split("|")

        val nf = parts[0].trim()
        if (parts.count() < 1 || parts[1].count() < 1) return GermanWordId(nf, PartOfSpeech.NonWord, null)
        val mods = parts[1].trim()

        val pos = when(mods[0]) {
            'n' -> PartOfSpeech.Noun
            'v' -> PartOfSpeech.Verb
            'j' -> PartOfSpeech.Adjective
            'a' -> PartOfSpeech.Adverb
            else -> throw Exception("Part of speech not supported: " + string)
        }

        var gender : Gender? = null
        if (pos == PartOfSpeech.Noun)
        {
            if (mods.count() != 2) throw Exception("Wrong format: " + string)
            gender = when(mods[1]) {
                'f' -> Gender.Feminine
                'm' -> Gender.Masculine
                'n' -> Gender.Neuter
                else -> throw Exception("Wrong format: " + string)
            }
        }
        return GermanWordId(nf, pos, gender)
    }
}