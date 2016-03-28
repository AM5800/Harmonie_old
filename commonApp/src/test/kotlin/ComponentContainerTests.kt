import am5800.common.componentContainer.ComponentContainer
import am5800.common.componentContainer.getComponent
import am5800.common.utils.Lifetime
import am5800.common.utils.TerminatedLifetimeException
import org.junit.Assert
import org.junit.Test

interface A
class B : A
class C(val value: Int)

class ComponentContainerTests {

  @Test(expected = TerminatedLifetimeException::class)
  fun testRegisterAfterLifetimeTermination() {
    val lifetime = Lifetime()
    val container = ComponentContainer(lifetime, null)
    lifetime.terminate()
    container.register(B())
  }

  @Test(expected = TerminatedLifetimeException::class)
  fun testGetComponentAfterLifetimeTermination() {
    val lifetime = Lifetime()
    val container = ComponentContainer(lifetime, null)
    container.register(B())
    lifetime.terminate()
    container.getComponent<A>()
  }

  @Test
  fun testParent() {
    Lifetime().use {
      val parent = ComponentContainer(it, null)
      val child = ComponentContainer(it, parent)

      parent.register(B())
      Assert.assertNotNull(child.getComponent<A>())
    }
  }

  @Test
  fun testChild() {
    Lifetime().use {
      val parent = ComponentContainer(it, null)
      val child = ComponentContainer(it, parent)

      child.register(B())
      Assert.assertNotNull(child.getComponent<A>())
    }
  }

  @Test(expected = Exception::class)
  fun testUnregistered() {
    Lifetime().use {
      ComponentContainer(it, null).getComponent<B>()
    }
  }

  @Test
  fun testChildAndParent() {
    Lifetime().use {
      val parent = ComponentContainer(it, null)
      val child = ComponentContainer(it, parent)

      child.register(C(0))
      parent.register(C(1))
      Assert.assertEquals(0, child.getComponent<C>().value)
      Assert.assertEquals(1, parent.getComponent<C>().value)
    }
  }
}