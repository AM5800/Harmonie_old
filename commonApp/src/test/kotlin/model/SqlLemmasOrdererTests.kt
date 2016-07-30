package model

import am5800.common.CommonLemma
import am5800.common.Lemma
import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.flow.SqlLemmasOrderer
import org.junit.Assert
import org.junit.Test
import testUtils.SentenceAndLemmasProviderMock
import testUtils.TestUserSqlDatabase


class SqlLemmasOrdererTests {
  private val sentenceAndLemmasProvider = SentenceAndLemmasProviderMock()
  private val lemma1 = CommonLemma("en:lemma1:Noun", 1)
  private val lemma2 = CommonLemma("en:lemma2:Noun", 2)
  private val lemma3 = CommonLemma("en:lemma3:Noun", 3)
  private val lemma4 = CommonLemma("en:lemma4:Noun", 4)

  private val allLemmas = listOf<Lemma>(lemma1, lemma2, lemma3, lemma4)

  init {
    sentenceAndLemmasProvider.lemmas.addAll(allLemmas)
  }

  @Test
  fun testEmptyOrdererDoesNotBreakInitialOrder() {
    Lifetime().use { lifetime ->
      val database = TestUserSqlDatabase(lifetime)
      val orderer = SqlLemmasOrderer(database, sentenceAndLemmasProvider)

      Assert.assertArrayEquals(allLemmas.toTypedArray(), orderer.reorder(allLemmas).toTypedArray())
    }
  }

  @Test
  fun testPullUpLast() {
    Lifetime().use { lifetime ->
      val database = TestUserSqlDatabase(lifetime)
      val orderer = SqlLemmasOrderer(database, sentenceAndLemmasProvider)
      orderer.pullUp(lemma4)

      Assert.assertArrayEquals(arrayOf(lemma4, lemma1, lemma2, lemma3), orderer.reorder(allLemmas).toTypedArray())
    }
  }

  @Test
  fun testRotate() {
    Lifetime().use { lifetime ->
      val database = TestUserSqlDatabase(lifetime)
      val orderer = SqlLemmasOrderer(database, sentenceAndLemmasProvider)
      orderer.pullUp(lemma2)
      orderer.pullUp(lemma3)
      orderer.pullUp(lemma4)

      Assert.assertArrayEquals(allLemmas.reversed().toTypedArray(), orderer.reorder(allLemmas).toTypedArray())
    }
  }

  @Test
  fun testRotateWithReload() {
    Lifetime().use { lifetime ->
      val database = TestUserSqlDatabase(lifetime)

      val orderer = SqlLemmasOrderer(database, sentenceAndLemmasProvider)
      orderer.pullUp(lemma2)
      orderer.pullUp(lemma3)
      orderer.pullUp(lemma4)

      val orderer2 = SqlLemmasOrderer(database, sentenceAndLemmasProvider)

      Assert.assertArrayEquals(allLemmas.reversed().toTypedArray(), orderer2.reorder(allLemmas).toTypedArray())
    }
  }
}

