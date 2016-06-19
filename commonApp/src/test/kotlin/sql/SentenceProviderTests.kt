package sql

import am5800.common.Language
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.services.SentencePair
import am5800.harmonie.app.model.services.impl.SqlSentence
import am5800.harmonie.app.model.services.impl.SqlSentenceProvider
import am5800.harmonie.app.model.services.impl.SqlWord
import org.junit.Assert
import org.junit.Test
import java.util.regex.Pattern


class SentenceProviderTests : DbTestBase() {
  private val key = "aufgabe"
  val sentenceProvider = SqlSentenceProvider(database, DebugOptions(false, false, 42))

  @Test
  fun testLanguagePairs() {
    val languagePairs = sentenceProvider.getAvailableLanguagePairs()
    val pair = languagePairs.single()
    Assert.assertEquals(Language.Russian, pair.entity.knownLanguage)
    Assert.assertEquals(Language.German, pair.entity.learnLanguage)
    Assert.assertEquals(2, pair.count)
  }

  @Test
  fun testWordsCount() {
    val sentence1 = SqlSentence(1, Language.German, "")
    Assert.assertEquals(9, sentenceProvider.getWordsInSentence(sentence1).size)

    val sentence2 = SqlSentence(2, Language.Russian, "")
    Assert.assertEquals(0, sentenceProvider.getWordsInSentence(sentence2).size)
  }

  @Test
  fun testKeyOccurrences() {
    val sentence = SqlSentence(3, Language.German, "")
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val keyOccurrence = occurrences.single { it.word.lemma == key }
    Assert.assertEquals(Language.English, keyOccurrence.word.language)
  }

  @Test(expected = Exception::class)
  fun testFoxOccurrencesWithWrongLanguage() {
    val sentence = SqlSentence(3, Language.Japanese, "")
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val keyOccurrence = occurrences.single { it.word.lemma == "key" }
    Assert.assertEquals(Language.English, keyOccurrence.word.language)
  }

  @Test
  fun getRandomSentenceWithNonExistentDirection() {
    val selectorResult = sentenceProvider.getRandomSentencePair(Language.Russian, Language.Japanese)
    Assert.assertNull(selectorResult)
  }

  @Test
  fun testRandomLanguagesConsistent() {
    val learnLanguage = Language.German
    val knownLanguage = Language.Russian
    repeat(10, {
      val result = sentenceProvider.getRandomSentencePair(learnLanguage, knownLanguage)
      Assert.assertNotNull(result)
      result!!
      Assert.assertEquals(learnLanguage, result.learnLanguageSentence.language)
      Assert.assertEquals(knownLanguage, result.knownLanguageSentence.language)
      // Test does not contains russian chars
      Assert.assertFalse(isRussian(result.learnLanguageSentence.text))
    })
  }

  private fun isRussian(text: String) = Pattern.matches("[а-яА-Я .]*", text)

  @Test
  fun getSentenceWithNonExistentDirection() {
    val key = getFoxWord()
    val result = sentenceProvider.findEasiestMatchingSentence(Language.Russian, Language.Japanese, listOf(key))
    Assert.assertNull(result)
  }

  private fun getFoxWord(): SqlWord {
    val query = "SELECT id FROM words WHERE lemma LIKE '$key' AND language='${Language.German.code}'"
    val cursor = database.query(query)
    cursor.moveToNext()
    val id = cursor.getString(0).toLong()
    return SqlWord(id, Language.German, key)
  }

  @Test
  fun testLanguagesConsistent() {
    val learnLanguage = Language.German
    val knownLanguage = Language.Russian
    val result = sentenceProvider.findEasiestMatchingSentence(learnLanguage, knownLanguage, listOf(getFoxWord()))
    Assert.assertNotNull(result)
    result!!
    Assert.assertEquals(learnLanguage, result.learnLanguageSentence.language)
    Assert.assertEquals(knownLanguage, result.knownLanguageSentence.language)
    // Test that sentence contains only english chars
    Assert.assertFalse(isRussian(result.learnLanguageSentence.text))
  }
}