package model

import am5800.harmonie.app.model.exercises.vplusp.SqlVPlusPDataProvider
import am5800.harmonie.app.model.sentencesAndLemmas.SqlSentenceAndLemmasProvider
import org.junit.Assert
import org.junit.Test
import testUtils.BaseTestWithLifetime
import testUtils.TestContentSqlDatabase

class VPlusPProviderTests : BaseTestWithLifetime() {
  private val contentDb = TestContentSqlDatabase(testClassLifetime)
  private val sentenceAndLemmasProvider = SqlSentenceAndLemmasProvider(contentDb)
  private val provider = SqlVPlusPDataProvider(contentDb, sentenceAndLemmasProvider)

  @Test
  fun testTopicsList() {
    val allTopics = provider.getAllTopics()
    Assert.assertArrayEquals(arrayOf("kreuzen auf"), allTopics.toTypedArray())
  }

  @Test
  fun testItemsExtraction() {
    val topic = provider.getAllTopics().single()
    val item = provider.get(topic).single()

    Assert.assertEquals(18, item.occurrenceStart)
    Assert.assertEquals(21, item.occurrenceEnd)
    Assert.assertEquals("test#2", item.sentence.uid)

  }
}