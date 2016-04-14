package sql

import am5800.common.Language
import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.services.impl.SqlSentenceProvider
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Test
import java.io.File


class SentenceProviderTests {
  val sentenceProvider = SqlSentenceProvider(TestSqlDatabase(lifetime, File("data\\test.db")))

  @Test
  fun testLanguagePairs() {
    val languagePairs = sentenceProvider.getAvailableLanguagePairs()
    val pair = languagePairs.single()
    Assert.assertEquals(Language.Russian, pair.entity.knownLanguage)
    Assert.assertEquals(Language.English, pair.entity.learnLanguage)
    Assert.assertEquals(2, pair.count)
  }

  companion object {
    val lifetime = Lifetime()

    @AfterClass
    @JvmStatic
    fun teardown() {
      lifetime.terminate()
    }
  }
}