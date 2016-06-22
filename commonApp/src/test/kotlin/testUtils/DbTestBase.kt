package testUtils

import am5800.common.utils.Lifetime
import org.junit.AfterClass
import org.junit.BeforeClass

open class DbTestBase {
  val testClassLifetime = Lifetime(lifetime)
  val database = TestSqlDatabase(testClassLifetime)

  companion object {
    private var lifetime : Lifetime? = null

    @BeforeClass
    @JvmStatic
    fun setup() {
      lifetime = Lifetime()
    }

    @AfterClass
    @JvmStatic
    fun teardown() {
      lifetime!!.terminate()
    }
  }
}