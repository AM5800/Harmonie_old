import com.google.common.collect.LinkedHashMultimap
import model.TextFormat
import common.words.Noun
import common.words.Word
import common.words.WordId
import org.junit.After
import org.junit.AfterClass
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import kotlin.test.assertEquals

public class TextFormatTests {
    private val fn = File("file.txt")

    @Test
    public fun test01() {
        val source = "Source"
        val wordId = WordId.create("noun|nm")
        val text = "This is text"
        val meaning = "meaning"

        val map = LinkedHashMultimap.create<String, Word>()
        map.put(source, Noun(wordId, listOf (meaning)))
        TextFormat.writeText(FileOutputStream(fn), text, map)

        assertEquals(text, TextFormat.readText(fn))
        val words = TextFormat.readWords(fn)
        assertEquals(1, words.count())
        assertEquals(wordId, words.first().id)
        assertEquals(meaning, words.first().meanings.first())
    }

    @Test
    public fun test02() {
        val text = "This is text"
        TextFormat.writeText(FileOutputStream(fn), text, null)
        assertEquals(text, TextFormat.readText(fn))
    }

    @Test
    public fun test03() {
        val text = "This is text"
        TextFormat.writeText(FileOutputStream(fn), text, null, 15)
        assertEquals(text, TextFormat.readText(fn))
    }


    @After
    public fun cleanup() {
        fn.delete()
    }
}