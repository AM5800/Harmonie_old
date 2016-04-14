package sql

import am5800.common.Language
import am5800.harmonie.app.model.services.impl.SqlSentence
import org.junit.Assert
import org.junit.Test
import java.util.regex.Pattern


class SentenceProviderTests : DbTestBase() {
  @Test
  fun testLanguagePairs() {
    val languagePairs = sentenceProvider.getAvailableLanguagePairs()
    val pair = languagePairs.single()
    Assert.assertEquals(Language.Russian, pair.entity.knownLanguage)
    Assert.assertEquals(Language.English, pair.entity.learnLanguage)
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
  fun getRandomSentence() {
    val learnLanguage = Language.English
    val knownLanguage = Language.Russian
    val selectorResult = sentenceProvider.getRandomSentencePair(learnLanguage, knownLanguage)
    Assert.assertNotNull(selectorResult)
    selectorResult!!
    Assert.assertEquals(learnLanguage, selectorResult.learnLanguageSentence.language)
    Assert.assertEquals(knownLanguage, selectorResult.knownLanguageSentence.language)
    // Test that sentence contains only english chars
    Assert.assertTrue(Pattern.matches("[a-zA-Z .]*", selectorResult.learnLanguageSentence.text))
  }
}