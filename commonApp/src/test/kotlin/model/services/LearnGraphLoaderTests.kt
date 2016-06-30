package model.services

import am5800.common.Language
import am5800.harmonie.app.model.services.SqlSentence
import am5800.harmonie.app.model.services.SqlWord
import am5800.harmonie.app.model.services.learnGraph.SqlLearnGraphLoader
import am5800.harmonie.app.model.services.sentencesAndWords.SqlSentenceAndWordsProvider
import org.junit.Assert
import org.junit.Test
import testUtils.DbTestBase


class LearnGraphLoaderTests : DbTestBase() {

  private val sqlSentenceAndWordsProvider = SqlSentenceAndWordsProvider(database)

  private val graphLoader = SqlLearnGraphLoader(database, sqlSentenceAndWordsProvider)

  @Test
  fun test() {
    val graph = graphLoader.load(Language.German).toTypedArray()
    Assert.assertEquals(9, graph.size)
    val expectedIdsMap = mapOf(Pair(0, 5L), Pair(4, 6L), Pair(7, 9L), Pair(8, 3L))
    val expectedSentencesMap = mapOf(Pair(8, listOf(1L)))

    for (i in 0..graph.size - 1) {
      val expectedId = expectedIdsMap[i] ?: continue
      val sqlWord = graph[i].word as SqlWord
      Assert.assertEquals(expectedId, sqlWord.id)
    }

    for (i in 0..graph.size - 1) {
      val expectedSentenceIds = expectedSentencesMap[i] ?: continue
      val sentenceIds = graph[i].sentences.map {it as SqlSentence }.map {it.id}.toTypedArray()
      Assert.assertArrayEquals(expectedSentenceIds.toTypedArray(), sentenceIds)
    }
  }
}