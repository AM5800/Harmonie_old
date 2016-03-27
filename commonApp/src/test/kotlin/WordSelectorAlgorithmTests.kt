import am5800.common.Language
import am5800.common.db.Word
import am5800.harmonie.app.model.WordSelectorAlgorithm
import am5800.harmonie.app.model.repetition.BinaryLearnScore
import org.junit.Assert
import org.junit.Test

data class WordScore(val word: Word, val score: BinaryLearnScore?)


class WordSelectorAlgorithmTests {
  val testWords = IntRange(0, 999).map { WordScore(Word(Language.English, it.toString()), null) }.toList()
  val defaultBaseSpeed = 36

  private fun selectNextWord(data: List<WordScore>, previousWord: Word?, averageScore: Double, baseSpeed: Int): Word? {
    val map = data.map { Pair(it.word, it.score) }.toMap()

    return WordSelectorAlgorithm.selectNextWord(data.map { it.word }, previousWord, averageScore, { map[it] }, baseSpeed)
  }

  @Test
  fun selectFirst() {
    val selected = selectNextWord(testWords, null, 0.0, defaultBaseSpeed)
    Assert.assertEquals(testWords.first().word, selected)
  }

  @Test
  fun testMiddleWordIsBad() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, BinaryLearnScore.Good)
    modifyScore(list, 10, BinaryLearnScore.Bad)
    val averageScore = computeAverageScore(list)

    val selected = selectNextWord(list, list.first().word, averageScore, 50)
    Assert.assertEquals(list[5].word, selected)
  }

  @Test
  fun testPrevIsNull() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, BinaryLearnScore.Good)

    val selected = selectNextWord(list, null, 1.0, 50)
    Assert.assertEquals(list[1].word, selected)
  }

  @Test
  fun prevWordDoesNotExist() {
    val selected = selectNextWord(testWords, Word(Language.German, "Doesnotexist"), 1.0, 10)
    Assert.assertEquals(testWords.first().word, selected)
  }

  @Test
  fun testSecondWordIsBad() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, BinaryLearnScore.Good)
    modifyScore(list, 1, BinaryLearnScore.Bad)
    val selected = selectNextWord(list, list.first().word, 0.5, defaultBaseSpeed)

    Assert.assertEquals(list[2].word, selected)
  }

  @Test
  fun testSecondWordIsBadAndNextIsGood() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, BinaryLearnScore.Good)
    modifyScore(list, 1, BinaryLearnScore.Bad)
    modifyScore(list, 2, BinaryLearnScore.Good)
    modifyScore(list, 3, BinaryLearnScore.Good)
    val selected = selectNextWord(list, list.first().word, computeAverageScore(list), defaultBaseSpeed)

    Assert.assertEquals(list[4].word, selected)
  }

  @Test
  fun testAllWordsInSpeedAreGood() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, BinaryLearnScore.Good)
    modifyScore(list, 1, BinaryLearnScore.Good)
    modifyScore(list, 2, BinaryLearnScore.Good)
    modifyScore(list, 3, BinaryLearnScore.Good)
    modifyScore(list, 4, BinaryLearnScore.Good)
    modifyScore(list, 5, BinaryLearnScore.Good)
    val selected = selectNextWord(list, list.first().word, computeAverageScore(list), 4)

    Assert.assertEquals(list[9].word, selected)
  }

  @Test
  fun testShortSequence() {
    val list = testWords.toMutableList().take(10).toMutableList()
    modifyScore(list, 0, BinaryLearnScore.Good)
    val selected = selectNextWord(list, list.first().word, 1.0, defaultBaseSpeed)
    Assert.assertEquals(list.last().word, selected)
  }

  @Test
  fun testShortSequenceAllGood() {
    val list = testWords.map { WordScore(it.word, BinaryLearnScore.Good) }
    val selected = selectNextWord(list, list.first().word, 1.0, defaultBaseSpeed)
    Assert.assertNull(selected)
  }

  @Test
  fun testMiddleWordIsGood() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, BinaryLearnScore.Good)
    modifyScore(list, defaultBaseSpeed / 4, BinaryLearnScore.Good)
    val averageScore = computeAverageScore(list)

    val selected = selectNextWord(list, list.first().word, averageScore, defaultBaseSpeed)
    Assert.assertEquals(list[defaultBaseSpeed + 1].word, selected)
  }

  @Test
  fun testSlowSpeed() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, BinaryLearnScore.Good)
    val selected = selectNextWord(list, list.first().word, 1.0, 1)

    Assert.assertEquals(list[1].word, selected)
  }

  private fun computeAverageScore(list: List<WordScore>): Double {
    return list.filter { it.score != null }.map {
      when (it.score) {
        BinaryLearnScore.Good -> 1.0
        BinaryLearnScore.Bad -> 0.0
        else -> throw Exception("Unexpected enum value: ${it.score}")
      }
    }.average()
  }

  private fun modifyScore(list: MutableList<WordScore>, index: Int, score: BinaryLearnScore) {
    list[index] = WordScore(list[index].word, score)
  }
}