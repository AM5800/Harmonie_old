package sql

import am5800.common.Language
import org.junit.Assert
import org.junit.Test


class SentenceProviderTests : DbTestBase() {
  @Test
  fun testLanguagePairs() {
    val languagePairs = sentenceProvider.getAvailableLanguagePairs()
    val pair = languagePairs.single()
    Assert.assertEquals(Language.Russian, pair.entity.knownLanguage)
    Assert.assertEquals(Language.English, pair.entity.learnLanguage)
    Assert.assertEquals(2, pair.count)
  }
}