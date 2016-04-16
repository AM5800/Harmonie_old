package am5800.harmonie.app.model.services.impl

import am5800.common.Language
import am5800.harmonie.app.model.features.repetition.WordsRepetitionService
import am5800.harmonie.app.model.services.SentencePair
import am5800.harmonie.app.model.services.SentenceSelector
import am5800.harmonie.app.model.services.WordSelector
import am5800.harmonie.app.model.services.logging.LoggerProvider
import org.joda.time.DateTime

class SqlSentenceSelector(private val repetitionService: WordsRepetitionService,
                          loggerProvider: LoggerProvider,
                          private val wordSelector: WordSelector,
                          private val sentenceProvider: SqlSentenceProvider) : SentenceSelector {

  private val logger = loggerProvider.getLogger(javaClass)

  override fun findBestSentenceByAttempts(learnLanguage: Language, knownLanguage: Language): SentencePair? {
    val scheduled = repetitionService.getScheduledWords(learnLanguage, DateTime.now()).filterIsInstance<SqlWord>()

    logger.info("Looking for best sentence. ${scheduled.size} words scheduled")

    if (!scheduled.isEmpty()) return sentenceProvider.findEasiestMatchingSentence(learnLanguage, knownLanguage, scheduled)

    val nextWord = wordSelector.findNextWord(learnLanguage) as? SqlWord
    logger.info("Next by frequency word is: ${nextWord?.lemma}")

    if (nextWord == null) return sentenceProvider.getRandomSentencePair(learnLanguage, knownLanguage)

    return sentenceProvider.findEasiestMatchingSentence(learnLanguage, knownLanguage, listOf(nextWord))
  }
}