package model

import am5800.common.Language
import am5800.harmonie.app.model.lemmasMeaning.SqlLemmaTranslationsProvider
import am5800.harmonie.app.model.sentencesAndLemmas.SqlSentenceAndLemmasProvider
import org.junit.Assert
import org.junit.Test
import testUtils.BaseTestWithLifetime
import testUtils.TestContentSqlDatabase


class SqlTranslationsProviderTests : BaseTestWithLifetime() {
  val database = TestContentSqlDatabase(testClassLifetime)
  val translationsProvider = SqlLemmaTranslationsProvider(database)
  val lemmasProvider = SqlSentenceAndLemmasProvider(database)
  val aufgabe = lemmasProvider.getAllLemmasSorted(Language.German).single { it.lemma == "aufgabe" }

  @Test
  fun testAufgabe() {
    val translations = translationsProvider.getTranslations(aufgabe, Language.Russian)
    Assert.assertArrayEquals(arrayOf("задача", "проблема"), translations.toTypedArray())
  }

  @Test
  fun testEmpty() {
    val translations = translationsProvider.getTranslations(aufgabe, Language.Japanese)
    Assert.assertEquals(0, translations.count())
  }
}