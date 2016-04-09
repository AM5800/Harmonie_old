import am5800.harmonie.app.model.features.flow.CategoryDistribution
import am5800.harmonie.app.model.features.flow.FlowItemCategory
import org.junit.Assert
import org.junit.Test
import java.util.*

class CategoryDistributionTests {
  private data class TestItemCategory(val id: Int) : FlowItemCategory

  @Test (expected = Exception::class)
  fun testEmpty() {
    CategoryDistribution(emptyMap())
  }

  @Test (expected = Exception::class)
  fun testDoesNotSumToOne() {
    CategoryDistribution(mapOf(Pair(TestItemCategory(0), 0.7), Pair(TestItemCategory(1), 0.5)))
  }

  @Test
  fun testOneZeroDistribution() {
    val distribution = CategoryDistribution(mapOf(Pair(TestItemCategory(0), 1.0), Pair(TestItemCategory(1), 0.0)))

    val random = Random()
    for (i in 1..1000) {
      Assert.assertEquals(TestItemCategory(0), distribution.getCategory(random.nextDouble()))
    }
  }

  @Test
  fun testTwoValues() {
    val distribution = CategoryDistribution(mapOf(Pair(TestItemCategory(0), 0.7), Pair(TestItemCategory(1), 0.3)))
    Assert.assertEquals(TestItemCategory(0), distribution.getCategory(0.5))
    Assert.assertEquals(TestItemCategory(1), distribution.getCategory(0.8))
  }
}