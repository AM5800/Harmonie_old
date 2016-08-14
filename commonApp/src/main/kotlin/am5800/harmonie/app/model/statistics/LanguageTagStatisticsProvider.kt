package am5800.harmonie.app.model.statistics

import am5800.common.Language
import am5800.harmonie.app.model.flow.FlowItemTag
import am5800.harmonie.app.model.flow.LanguageTag
import am5800.harmonie.app.model.repetition.LemmaRepetitionService
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.model.workspace.TagStatisticsProvider
import org.joda.time.DateTime


class LanguageTagStatisticsProvider(private val lemmaRepetitionService: LemmaRepetitionService,
                                    private val sentenceAndLemmasProvider: SentenceAndLemmasProvider) : TagStatisticsProvider {
  override fun getTotalCount(tags: Collection<FlowItemTag>): Int {
    return count(tags, {
      sentenceAndLemmasProvider.getAllLemmasSorted(it).size
    })
  }

  override fun getOnDueCount(tags: Collection<FlowItemTag>): Int {
    return count(tags, {
      lemmaRepetitionService.countOnDueLemmas(it, DateTime.now())
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