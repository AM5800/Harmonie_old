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

  @Test
  fun test() {
    val translationsProvider = SqlLemmaTranslationsProvider(database)
    val lemmasProvider = SqlSentenceAndLemmasProvider(database)

    val aufgabe = lemmasProvider.getAllLemmasSorted(Language.German).single { it.lemma == "aufgabe" }

    val translations = translationsProvider.getTranslations(aufgabe, Language.Russian)
    Assert.assertArrayEquals(arrayOf("задача", "проблема"), translations.toTypedArray())
  }
}