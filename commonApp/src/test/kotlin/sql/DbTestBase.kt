package sql

import am5800.common.utils.Lifetime
import org.junit.AfterClass

open class DbTestBase {
  val database = TestSqlDatabase(lifetime)

  companion object {
    val lifetime = Lifetime()

    @AfterClass
    @JvmStatic
    fun teardown() {
      lifetime.terminate()
    }
  }
}