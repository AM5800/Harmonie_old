package sql

import am5800.common.Language
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.services.impl.SqlSentence
import am5800.harmonie.app.model.services.impl.SqlSentenceProvider
import am5800.harmonie.app.model.services.impl.SqlWord
import org.junit.Assert
import org.junit.Test
import java.util.regex.Pattern


class SentenceProviderTests : DbTestBase() {
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
    val sentence1 = SqlSentence(1, Language.Japanese, "")
    Assert.assertEquals(5, sentenceProvider.getWordsInSentence(sentence1).size)

    val sentence2 = SqlSentence(2, Language.Russian, "")
    Assert.assertEquals(0, sentenceProvider.getWordsInSentence(sentence2).size)
  }

  @Test
  fun testFoxOccurrences() {
    val sentence = SqlSentence(3, Language.English, "")
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val foxOccurrence = occurrences.single { it.word.lemma == "fox" }
    Assert.assertEquals(Language.English, foxOccurrence.word.language)
  }

  @Test(expected = Exception::class)
  fun testFoxOccurrencesWithWrongLanguage() {
    val sentence = SqlSentence(3, Language.Japanese, "")
    val occurrences = sentenceProvider.getOccurrences(sentence)
    val foxOccurrence = occurrences.single { it.word.lemma == "fox" }
    Assert.assertEquals(Language.English, foxOccurrence.word.language)
  }

  @Test
  fun getRandomSentenceWithNonExistentDirection() {
    val selectorResult = sentenceProvider.getRandomSentencePair(Language.Russian, Language.Japanese)
    Assert.assertNull(selectorResult)
  }

  @Test
  fun testRandomLanguagesConsistent() {
    val learnLanguage = Language.English
    val knownLanguage = Language.Russian
    val result = sentenceProvider.getRandomSentencePair(learnLanguage, knownLanguage)
    Assert.assertNotNull(result)
    result!!
    Assert.assertEquals(learnLanguage, result.learnLanguageSentence.language)
    Assert.assertEquals(knownLanguage, result.knownLanguageSentence.language)
    // Test that sentence contains only english chars
    Assert.assertTrue(Pattern.matches("[a-zA-Z .]*", result.learnLanguageSentence.text))
  }

  @Test
  fun getSentenceWithNonExistentDirection() {
    val fox = getFoxWord()
    val result = sentenceProvider.findEasiestMatchingSentence(Language.Russian, Language.Japanese, listOf(fox))
    Assert.assertNull(result)
  }

  private fun getFoxWord(): SqlWord {
    val query = "SELECT id FROM words WHERE lemma LIKE 'fox' AND language='${Language.English.code}'"
    val cursor = database.query(query)
    cursor.moveToNext()
    val id = cursor.getString(0).toLong()
    return SqlWord(id, Language.English, "fox")
  }

  @Test
  fun testLanguagesConsistent() {
    val learnLanguage = Language.English
    val knownLanguage = Language.Russian
    val result = sentenceProvider.findEasiestMatchingSentence(learnLanguage, knownLanguage, listOf(getFoxWord()))
    Assert.assertNotNull(result)
    result!!
    Assert.assertEquals(learnLanguage, result.learnLanguageSentence.language)
    Assert.assertEquals(knownLanguage, result.knownLanguageSentence.language)
    // Test that sentence contains only english chars
    Assert.assertTrue(Pattern.matches("[a-zA-Z .]*", result.learnLanguageSentence.text))
  }
}