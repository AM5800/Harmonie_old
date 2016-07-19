package am5800.harmonie.app.model.features.statistics

import am5800.common.Language
import am5800.harmonie.app.model.features.repetition.WordsRepetitionService
import am5800.harmonie.app.model.services.flow.FlowItemTag
import am5800.harmonie.app.model.services.flow.LanguageTag
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndWordsProvider
import am5800.harmonie.app.model.services.workspace.TagStatisticsProvider
import org.joda.time.DateTime


class LanguageTagStatisticsProvider(private val wordsRepetitionService: WordsRepetitionService,
                                    private val sentenceAndWordsProvider: SentenceAndWordsProvider) : TagStatisticsProvider {
  override fun getTotalCount(tags: Collection<FlowItemTag>): Int {
    return count(tags, {
      sentenceAndWordsProvider.getAllWords(it).size
    })
  }

  override fun getOnDueCount(tags: Collection<FlowItemTag>): Int {
    return count(tags, {
      wordsRepetitionService.countAllScheduledWords(it, DateTime.now())
    })
  }

  override fun getOnLearningCount(tags: Collection<FlowItemTag>): Int {
    return count(tags, {
      wordsRepetitionService.getAttemptedWords(it).size
    })
  }

  private fun count(tags: Collection<FlowItemTag>, counter: (Language) -> Int): Int {
    return tags.filterIsInstance<LanguageTag>().distinct().map { it.learnLanguage }.fold(0, { i, language ->
      i + counter(language)
    })
  }
}