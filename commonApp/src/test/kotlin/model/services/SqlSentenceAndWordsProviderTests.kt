package model.services

import TestDebugOptions
import am5800.common.Language
import am5800.harmonie.app.model.services.SqlSentence
import am5800.harmonie.app.model.services.SqlWord
import am5800.harmonie.app.model.services.flow.Competence
import am5800.harmonie.app.model.services.flow.LanguageCompetence
import am5800.harmonie.app.model.services.sentencesAndWords.SqlSentenceAndWordsProvider
import org.junit.Assert
import org.junit.Test
import testUtils.DbTestBase


class SqlSentenceAndWordsProviderTests : DbTestBase() {
  private val key = "aufgabe"
  val sentenceProvider = SqlSentenceAndWordsProvider(database, TestDebugOptions.instance)
  val sentences = listOf(
      SqlSentence(1, Language.German, ""),
      SqlSentence(2, Language.Russian, ""),
      SqlSentence(3, Language.German, ""),
      SqlSentence(4, Language.Russian, ""),
      SqlSentence(5, Language.German, ""),
      SqlSentence(6, Language.Russian, "")
  )

  @Test
  fun testWordsCount() {
    val sentence1 = sentences.single { it.id == 1L }
    Assert.assertEquals(9, sentenceProvider.getWordsInSentence(sentence1).size)

    val sentence2 = sentences.single { it.id == 2L }
    Assert.assertEquals(0, sentenceProvider.getWordsInSentence(sentence2).size)
  }

  @Test
  fun testKeyOccurrences() {
    val sentence = sentences.single { it.id == 1L }
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val keyOccurrence = occurrences.single { it.word.lemma == key }
    Assert.assertEquals(Language.German, keyOccurrence.word.language)
  }

  @Test(expected = Exception::class)
  fun testKeyOccurrencesWithWrongLanguage() {
    val sentence = SqlSentence(1, Language.Japanese, "")
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val keyOccurrence = occurrences.single { it.word.lemma == key }
    Assert.assertEquals(Language.English, keyOccurrence.word.language)
  }

  private fun getCompetence(language: Language): List<LanguageCompetence> = listOf(LanguageCompetence(language, Competence.Native))

  @Test
  fun testGetSentenceWithFilter() {
    val word = getWord(key)
    val result = sentenceProvider.getEasiestRandomSentenceWith(word, getCompetence(Language.Russian))!!
    val learnLanguageSentence = result.sentence as SqlSentence
    Assert.assertEquals(1L, learnLanguageSentence.id)
  }

  @Test()
  fun testGetSentenceWithInconsistentFilter() {
    val word = getWord("mein")
    val result = sentenceProvider.getEasiestRandomSentenceWith(word, getCompetence(Language.Japanese))
    Assert.assertNull(result)
  }

  private fun getWord(word: String): SqlWord {
    val allWords = sentenceProvider.getAllWords(Language.German)
    return allWords.single {it.value.lemma == word}.value as SqlWord
  }
}