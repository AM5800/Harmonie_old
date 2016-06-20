package sql

import am5800.common.Language
import am5800.harmonie.app.model.services.languagePairs.SqlLanguagePairsProvider
import org.junit.Assert
import org.junit.Test


class LanguagePairsProviderTests : DbTestBase() {
  @Test
  fun test() {
    val languagePairs = SqlLanguagePairsProvider(database).getAvailableLanguagePairs()
    val pair = languagePairs.single()
    Assert.assertEquals(Language.Russian, pair.entity.knownLanguage)
    Assert.assertEquals(Language.German, pair.entity.learnLanguage)
    Assert.assertEquals(3, pair.count)
  }
}