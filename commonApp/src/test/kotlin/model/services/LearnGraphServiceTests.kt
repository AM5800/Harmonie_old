package model.services

import am5800.common.Language
import am5800.common.LearnGraphNode
import am5800.common.Sentence
import am5800.common.Word
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.convert
import am5800.harmonie.app.model.services.learnGraph.LearnGraphService
import am5800.harmonie.app.model.services.learnGraph.LearnGraphServiceImpl
import org.junit.Assert
import org.junit.Test
import testUtils.DbTestBase
import testUtils.KeyValueDatabaseMock
import testUtils.LearnGraphLoaderMock

class LearnGraphServiceTests : DbTestBase() {
  private val language = Language.German
  private fun createService(graph: List<LearnGraphNode>, position: Property<Int>): LearnGraphService {
    val dbMock = KeyValueDatabaseMock()
    dbMock.addPropertyForKey(LearnGraphServiceImpl.settingsKey + ":de", position.convert({ it.toString() }, { it.toInt() }))
    val loader = LearnGraphLoaderMock(graph)
    return LearnGraphServiceImpl(loader, dbMock, testClassLifetime)
  }

  private fun node(wordId: String, vararg sentenceIds: String): LearnGraphNode {
    val word = Word(language, wordId)
    val sentences = sentenceIds.map { Sentence(language, it) }
    return LearnGraphNode(word, sentences)
  }

  private fun getWordIds(words: List<Word>): Array<String> {
    return words.map { it.lemma }.toTypedArray()
  }

  private fun getSentenceIds(sentences: List<Sentence>): Array<String> {
    return sentences.map { it.text }.toTypedArray()
  }

  @Test
  fun newGraph() {
    val graph = listOf(node("W1"), node("W2", "S1"), node("W3"), node("W4", "S2"))
    val index = Property(testClassLifetime, 0)
    val service = createService(graph, index)
    service.canUnlockNextWordGroup(language)

    Assert.assertEquals(index.value, 1)
    Assert.assertArrayEquals(arrayOf("W1", "W2"), getWordIds(service.getUnlockedWords(language)))
    Assert.assertArrayEquals(arrayOf("S1"), getSentenceIds(service.getUnlockedSentences(language)))
  }

  @Test
  fun testUnlockingGraph() {
    val graph = listOf(node("W1"), node("W2", "S1"), node("W3"), node("W4", "S2"))
    val index = Property(testClassLifetime, 0)
    val service = createService(graph, index)

    Assert.assertTrue(service.canUnlockNextWordGroup(language))
    val newWords = service.unlockNextWordsGroup(language)

    Assert.assertArrayEquals(arrayOf("W3", "W4"), getWordIds(newWords))

    Assert.assertArrayEquals(arrayOf("W1", "W2", "W3", "W4"), getWordIds(service.getUnlockedWords(language)))
    Assert.assertArrayEquals(arrayOf("S1", "S2"), getSentenceIds(service.getUnlockedSentences(language)))

    Assert.assertFalse(service.canUnlockNextWordGroup(language))
  }
}