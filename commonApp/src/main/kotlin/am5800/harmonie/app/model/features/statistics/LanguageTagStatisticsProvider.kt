package am5800.harmonie.app.model.features.statistics

import am5800.common.Language
import am5800.harmonie.app.model.features.repetition.LemmaRepetitionService
import am5800.harmonie.app.model.services.flow.FlowItemTag
import am5800.harmonie.app.model.services.flow.LanguageTag
import am5800.harmonie.app.model.services.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.model.services.workspace.TagStatisticsProvider
import org.joda.time.DateTime


class LanguageTagStatisticsProvider(private val lemmaRepetitionService: LemmaRepetitionService,
                                    private val sentenceAndLemmasProvider: SentenceAndLemmasProvider) : TagStatisticsProvider {
  override fun getTotalCount(tags: Collection<FlowItemTag>): Int {
    return count(tags, {
      sentenceAndLemmasProvider.getAllLemmas(it).size
    })
  }

  override fun getOnDueCount(tags: Collection<FlowItemTag>): Int {
    return count(tags, {
      lemmaRepetitionService.countAllScheduledLemmas(it, DateTime.now())
    })
  }

  override fun getOnLearningCount(tags: Collection<FlowItemTag>): Int {
    return count(tags, {
      lemmaRepetitionService.getAttemptedLemmas(it).size
    })
  }

  private fun count(tags: Collection<FlowItemTag>, counter: (Language) -> Int): Int {
    return tags.filterIsInstance<LanguageTag>().distinct().map { it.learnLanguage }.fold(0, { i, language ->
      i + counter(language)
    })
  }
}