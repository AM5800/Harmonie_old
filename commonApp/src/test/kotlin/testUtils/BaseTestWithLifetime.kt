package testUtils

import am5800.common.utils.Lifetime
import org.junit.AfterClass
import org.junit.BeforeClass

open class BaseTestWithLifetime {
  val testClassLifetime = Lifetime(lifetime)

  companion object {
    private var lifetime: Lifetime? = null

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