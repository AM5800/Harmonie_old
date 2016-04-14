package sql

import am5800.common.utils.Lifetime
import am5800.harmonie.app.model.services.impl.SqlSentenceProvider
import org.junit.AfterClass

open class DbTestBase {
  val database = TestSqlDatabase(lifetime)

  val sentenceProvider = SqlSentenceProvider(database)

  companion object {
    val lifetime = Lifetime()

    @AfterClass
    @JvmStatic
    fun teardown() {
      lifetime.terminate()
    }
  }
}