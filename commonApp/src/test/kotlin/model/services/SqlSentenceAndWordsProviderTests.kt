package model.services

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
  val sentenceProvider = SqlSentenceAndWordsProvider(database)
  val sentences = listOf(
      SqlSentence(1, Language.German, "", null),
      SqlSentence(2, Language.Russian, "", null),
      SqlSentence(3, Language.German, "", null),
      SqlSentence(4, Language.Russian, "", null),
      SqlSentence(5, Language.German, "", null),
      SqlSentence(6, Language.Russian, "", null)
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
    Assert.assertNull(sentence2.uid)
  }

  @Test
  fun testWordsCount() {
    val sentence1 = sentences.single { it.sqlId == 1L }
    Assert.assertEquals(9, sentenceProvider.getWordsInSentence(sentence1).size)

    val sentence2 = sentences.single { it.sqlId == 2L }
    Assert.assertEquals(0, sentenceProvider.getWordsInSentence(sentence2).size)
  }

  @Test
  fun testKeyOccurrences() {
    val sentence = sentences.single { it.sqlId == 1L }
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val keyOccurrence = occurrences.single { it.word.lemma == key }
    Assert.assertEquals(Language.German, keyOccurrence.word.language)
  }

  @Test(expected = Exception::class)
  fun testKeyOccurrencesWithWrongLanguage() {
    val sentence = SqlSentence(1, Language.Japanese, "", null)
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val keyOccurrence = occurrences.single { it.word.lemma == key }
    Assert.assertEquals(Language.English, keyOccurrence.word.language)
  }

  private fun getCompetence(language: Language): List<LanguageCompetence> = listOf(LanguageCompetence(language, Competence.Native))

  @Test()
  fun testGetSentenceWithInconsistentFilter() {
    val word = getWord("mein")
    val result = sentenceProvider.getEasiestSentencesWith(word, getCompetence(Language.Japanese), 50)
    Assert.assertEquals(0, result.size)
  }

  private fun getWord(word: String): SqlWord {
    val allWords = sentenceProvider.getAllWords(Language.German)
    return allWords.single { it.value.lemma == word }.value as SqlWord
  }
}