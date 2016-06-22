package model.services

import am5800.common.Language
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.services.SqlSentence
import am5800.harmonie.app.model.services.sentencesAndWords.SqlSentenceAndWordsProvider
import am5800.harmonie.app.model.services.SqlWord
import org.junit.Assert
import org.junit.Test
import testUtils.DbTestBase


class SqlSentenceAndWordsProviderTests : DbTestBase() {
  private val key = "aufgabe"
  val sentenceProvider = SqlSentenceAndWordsProvider(database)
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

  @Test
  fun testGetSentenceWithFilter() {
    val germanSentences = sentences.filter { it.language == Language.German }.take(1)
    val word = getWord(key)
    val result = sentenceProvider.getRandomSentenceWith(word, Language.Russian, germanSentences)!!
    val learnLanguageSentence = result.learnLanguageSentence as SqlSentence
    Assert.assertEquals(1L, learnLanguageSentence.id)
  }

  @Test()
  fun testGetSentenceWithInconsistentFilter() {
    val germanSentences = sentences.filter { it.language == Language.German }.take(2)
    val word = getWord("mein")
    val result = sentenceProvider.getRandomSentenceWith(word, Language.Russian, germanSentences)
    Assert.assertNull(result)
  }

  private fun getWord(word: String): SqlWord {
    val query = "SELECT id FROM words WHERE lemma LIKE '$word' AND language='${Language.German.code}'"
    val cursor = database.query(query)
    cursor.moveToNext()
    val id = cursor.getString(0).toLong()
    return SqlWord(id, Language.German, word)
  }
}