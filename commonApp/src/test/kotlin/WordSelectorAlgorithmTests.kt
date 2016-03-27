import am5800.common.Language
import am5800.common.db.Word
import am5800.harmonie.app.model.repetition.LearnScore
import am5800.harmonie.app.model.WordScore
import am5800.harmonie.app.model.WordSelectorAlgorithm
import org.junit.Assert
import org.junit.Test

class WordSelectorAlgorithmTests {
  val testWords = IntRange(0, 999).map { WordScore(Word(Language.English, it.toString()), LearnScore.Unknown) }.toList()
  val defaultBaseSpeed = 36

  @Test
  fun selectFirst() {
    val selected = WordSelectorAlgorithm.selectNextWord(testWords, null, 0.0)
    Assert.assertEquals(testWords.first().word, selected)
  }

  @Test
  fun testMiddleWordIsBad() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, LearnScore.Good)
    modifyScore(list, 10, LearnScore.Bad)
    val averageScore = computeAverageScore(list)

    val selected = WordSelectorAlgorithm.selectNextWord(list, list.first().word, averageScore, 50)
    Assert.assertEquals(list[5].word, selected)
  }

  @Test
  fun testSecondWordIsBad() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, LearnScore.Good)
    modifyScore(list, 1, LearnScore.Bad)
    val selected = WordSelectorAlgorithm.selectNextWord(list, list.first().word, 0.5, defaultBaseSpeed)

    Assert.assertEquals(list[2].word, selected)
  }

  @Test
  fun testSecondWordIsBadAndNextIsGood() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, LearnScore.Good)
    modifyScore(list, 1, LearnScore.Bad)
    modifyScore(list, 2, LearnScore.Good)
    modifyScore(list, 3, LearnScore.Good)
    val selected = WordSelectorAlgorithm.selectNextWord(list, list.first().word, computeAverageScore(list), defaultBaseSpeed)

    Assert.assertEquals(list[4].word, selected)
  }

  @Test
  fun testAllWordsInSpeedAreGood() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, LearnScore.Good)
    modifyScore(list, 1, LearnScore.Good)
    modifyScore(list, 2, LearnScore.Good)
    modifyScore(list, 3, LearnScore.Good)
    modifyScore(list, 4, LearnScore.Good)
    modifyScore(list, 5, LearnScore.Good)
    val selected = WordSelectorAlgorithm.selectNextWord(list, list.first().word, computeAverageScore(list), 4)

    Assert.assertEquals(list[9].word, selected)
  }

  @Test
  fun testShortSequence() {
    val list = testWords.toMutableList().take(10).toMutableList()
    modifyScore(list, 0, LearnScore.Good)
    val selected = WordSelectorAlgorithm.selectNextWord(list, list.first().word, 1.0, defaultBaseSpeed)
    Assert.assertEquals(list.last().word, selected)
  }

  @Test
  fun testShortSequenceAllGood() {
    val list = testWords.map { WordScore(it.word, LearnScore.Good) }
    val selected = WordSelectorAlgorithm.selectNextWord(list, list.first().word, 1.0, defaultBaseSpeed)
    Assert.assertNull(selected)
  }

  @Test
  fun testMiddleWordIsGood() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, LearnScore.Good)
    modifyScore(list, defaultBaseSpeed / 4, LearnScore.Good)
    val averageScore = computeAverageScore(list)

    val selected = WordSelectorAlgorithm.selectNextWord(list, list.first().word, averageScore, defaultBaseSpeed)
    Assert.assertEquals(list[defaultBaseSpeed + 1].word, selected)
  }

  @Test
  fun testSlowSpeed() {
    val list = testWords.toMutableList()
    modifyScore(list, 0, LearnScore.Good)
    val selected = WordSelectorAlgorithm.selectNextWord(list, list.first().word, 1.0, 1)

    Assert.assertEquals(list[1].word, selected)
  }

  private fun computeAverageScore(list: List<WordScore>): Double {
    return list.filter { it.score != LearnScore.Unknown }.map {
      when (it.score) {
        LearnScore.Good -> 1.0
        LearnScore.Bad -> 0.0
        else -> throw Exception("Unexpected enum value: ${it.score}")
      }
    }.average()
  }

  private fun modifyScore(list: MutableList<WordScore>, index: Int, score: LearnScore) {
    list[index] = WordScore(list[index].word, score)
  }
}