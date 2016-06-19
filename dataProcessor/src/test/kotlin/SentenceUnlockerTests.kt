import am5800.common.Language
import am5800.common.Sentence
import am5800.common.Word
import am5800.common.WordOccurrence
import dataProcessor.UnlockInfo
import dataProcessor.SentenceUnlocker
import org.junit.Assert
import org.junit.Test

class SentenceUnlockerTests {
  private val language = Language.German
  private val counts = mapOf<Word, Int>(
      Pair(Word(language, "100"), 100),
      Pair(Word(language, "10"), 10),
      Pair(Word(language, "1"), 1),
      Pair(Word(language, "50"), 50),
      Pair(Word(language, "30"), 30)
  )

  private fun createOccurrences(sentenceText: String, vararg wordsInSentence: String): List<WordOccurrence> {
    val sentence = Sentence(language, sentenceText)
    val occurrences = wordsInSentence.mapIndexed { i, s -> WordOccurrence(Word(language, s), sentence, i, i + 1) }
    return occurrences
  }

  @Test
  fun test1() {
    val occurrences = mutableListOf<WordOccurrence>()
    occurrences.addAll(createOccurrences("FIRST", "1", "100", "1"))
    occurrences.addAll(createOccurrences("SECOND", "30", "10"))
    val actual = SentenceUnlocker.createUnlockOrder(counts, language, occurrences)
    val expected = listOf(makeUi("30"), makeUi("10", "SECOND"), makeUi("100"), makeUi("1", "FIRST"))
    Assert.assertArrayEquals(expected.toTypedArray(), actual.toTypedArray())
  }

  private fun makeUi(word: String, vararg sentences: String): UnlockInfo {
    return UnlockInfo(Word(language, word), sentences.map { Sentence(language, it) })
  }

  @Test
  fun test2() {
    val occurrences = mutableListOf<WordOccurrence>()
    occurrences.addAll(createOccurrences("FIRST", "100", "1"))
    occurrences.addAll(createOccurrences("SECOND", "50", "1"))
    occurrences.addAll(createOccurrences("THIRD", "30", "1"))
    occurrences.addAll(createOccurrences("FOURTH", "10", "1"))
    val actual = SentenceUnlocker.createUnlockOrder(counts, language, occurrences)
    val expected = listOf(makeUi("100"), makeUi("50"), makeUi("30"), makeUi("10"), makeUi("1", "FIRST", "SECOND", "THIRD", "FOURTH"))
    Assert.assertArrayEquals(expected.toTypedArray(), actual.toTypedArray())
  }
}