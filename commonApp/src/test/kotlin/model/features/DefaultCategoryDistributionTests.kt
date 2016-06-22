package model.features

import am5800.common.Language
import am5800.harmonie.app.model.features.fillTheGap.FillTheGapCategory
import am5800.harmonie.app.model.features.flow.createDefaultCategoryDistribution
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceCategory
import org.junit.Assert
import org.junit.Test

class DefaultCategoryDistributionTests {
  @Test
  fun testParallelSentencesHasBiggerPriority() {
    val item1 = FillTheGapCategory(Language.German, Language.Russian)
    val item2 = FillTheGapCategory(Language.Russian, Language.German)
    val item3 = ParallelSentenceCategory(Language.German, Language.Russian)
    val item4 = ParallelSentenceCategory(Language.German, Language.Russian)
    val item5 = ParallelSentenceCategory(Language.English, Language.Russian)

    val distribution = createDefaultCategoryDistribution(listOf(item1, item2, item3, item4, item5))

    Assert.assertEquals(0.333, distribution[item3]!!, 0.001)
  }

  @Test
  fun testIsDistribution() {
    val item1 = FillTheGapCategory(Language.German, Language.Russian)
    val item2 = FillTheGapCategory(Language.Russian, Language.German)
    val item3 = ParallelSentenceCategory(Language.German, Language.Russian)

    val distribution = createDefaultCategoryDistribution(listOf(item1, item2, item3))
    Assert.assertEquals(1.0, distribution.map {it.value}.sum(), 0.0001)
  }

}