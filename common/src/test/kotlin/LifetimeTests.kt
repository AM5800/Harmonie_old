import am5800.common.utils.Lifetime
import org.junit.Assert
import org.junit.Test

class LifetimeTests {
  @Test
  fun testNesting() {
    val parent = Lifetime()
    val child = Lifetime(parent)

    Assert.assertFalse(child.isTerminated)
    parent.terminate()
    Assert.assertTrue(child.isTerminated)
  }

  @Test
  fun testTerminatedNesting() {
    val parent = Lifetime()
    parent.terminate()
    val child = Lifetime(parent)
    Assert.assertTrue(child.isTerminated)
  }

  @Test
  fun testCallbacks() {
    val lifetime = Lifetime()
    var i = 0
    lifetime.addAction { ++i }
    lifetime.terminate()

    Assert.assertEquals(1, i)
  }

  @Test
  fun testTerminatedCallbacks() {
    val lifetime = Lifetime()
    var i = 0
    lifetime.terminate()
    val added = lifetime.tryAddAction { ++i }

    Assert.assertEquals(0, i)
    Assert.assertFalse(added)
  }
}