package model

import am5800.common.Language
import am5800.common.PartOfSpeech
import am5800.harmonie.app.model.languageCompetence.Competence
import am5800.harmonie.app.model.languageCompetence.LanguageCompetence
import am5800.harmonie.app.model.sentencesAndLemmas.SqlSentenceAndLemmasProvider
import am5800.harmonie.app.model.sql.SqlLemma
import am5800.harmonie.app.model.sql.SqlSentence
import org.junit.Assert
import org.junit.Test
import testUtils.BaseTestWithLifetime
import testUtils.TestContentSqlDatabase


class SqlSentenceAndLemmasProviderTests : BaseTestWithLifetime() {
  val database = TestContentSqlDatabase(testClassLifetime)
  private val key = "aufgabe"
  val sentenceProvider = SqlSentenceAndLemmasProvider(database)
  val sentences = listOf(
      SqlSentence(1, Language.German, "", "id", 0),
      SqlSentence(2, Language.Russian, "", "id", 0),
      SqlSentence(3, Language.German, "", "id", 0),
      SqlSentence(4, Language.Russian, "", "id", 0),
      SqlSentence(5, Language.German, "", "id", 0),
      SqlSentence(6, Language.Russian, "", "id", 0)
  )

  @Test
  fun testSentenceReading() {
    val sentence3 = sentenceProvider.getSentencesFlat(listOf(3L)).single()
    Assert.assertEquals("Kreuzen Sie bitte auf dem Antwortbogen an.", sentence3.text)
    Assert.assertEquals("test#2", sentence3.uid)
    Assert.assertEquals(Language.German, sentence3.language)

    val sentence2 = sentenceProvider.getSentencesFlat(listOf(2L)).single()
    Assert.assertEquals("Не важно", sentence2.text)
    Assert.assertEquals(Language.Russian, sentence2.language)
    Assert.assertEquals("test#1", sentence2.uid)
  }

  @Test
  fun testKeyOccurrences() {
    val sentence = sentences.single { it.sqlId == 1L }
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val keyOccurrence = occurrences.single { it.lemma.lemma == key }
    Assert.assertEquals(Language.German, keyOccurrence.lemma.language)
  }

  @Test(expected = Exception::class)
  fun testKeyOccurrencesWithWrongLanguage() {
    val sentence = SqlSentence(1, Language.Japanese, "", "id", 0)
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val keyOccurrence = occurrences.single { it.lemma.lemma == key }
    Assert.assertEquals(Language.English, keyOccurrence.lemma.language)
  }

  private fun getCompetence(language: Language): List<LanguageCompetence> = listOf(LanguageCompetence(language, Competence.Native))

  @Test()
  fun testGetSentenceWithInconsistentFilter() {
    val lemma = SqlLemma(100, "en:lemma:Other", 0)
    val result = sentenceProvider.getEasiestSentencesWith(lemma, getCompetence(Language.Japanese), 50)
    Assert.assertEquals(0, result.size)
  }

  @Test()
  fun testLemmasReading() {
    val allLemmas = sentenceProvider.getAllLemmasSorted(Language.German)
    Assert.assertEquals(9, allLemmas.size)

    val keyed = allLemmas.single { it.lemma == key }
    Assert.assertEquals(0, keyed.difficultyLevel)
    Assert.assertEquals(Language.German, keyed.language)
    Assert.assertEquals(PartOfSpeech.Other, keyed.partOfSpeech)
  }

  @Test
  fun testLemmasSearch1() {
    val lemmas = sentenceProvider.searchLemmas("i", Language.German)
    Assert.assertEquals(3, lemmas.size)
  }

  @Test
  fun testLemmasSearchAll() {
    val lemmas = sentenceProvider.searchLemmas("", Language.German)
    Assert.assertEquals(9, lemmas.size)
  }

  @Test
  fun testLemmasSearchZ() {
    val lemmas = sentenceProvider.searchLemmas("z", Language.German)
    Assert.assertEquals(0, lemmas.size)
  }
}