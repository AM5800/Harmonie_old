package am5800.harmonie.app.model.services.sentenceSelection

import am5800.common.Language
import am5800.common.WithLevel
import am5800.common.Word
import am5800.common.utils.EnumerableDistribution
import am5800.common.utils.functions.random
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.flow.LanguageCompetence
import am5800.harmonie.app.model.features.repetition.WordsRepetitionService
import am5800.harmonie.app.model.services.logging.LoggerProvider
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndTranslation
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndWordsProvider
import org.joda.time.DateTime

class SentenceSelectionStrategyImpl(private val repetitionService: WordsRepetitionService,
                                    private val debugOptions: DebugOptions,
                                    loggerProvider: LoggerProvider,
                                    private val sentenceAndWordsProvider: SentenceAndWordsProvider) : SentenceSelectionStrategy {

  private enum class NextTask {
    RepeatRandomWord, LearnNewWord
  }

  private val nextTaskDistribution = EnumerableDistribution.define<NextTask> {
    add(NextTask.LearnNewWord, 0.7)
    addRest(NextTask.RepeatRandomWord)
  }

  private val logger = loggerProvider.getLogger(javaClass)

  override fun findBestSentenceByAttempts(learnLanguage: Language, competence: List<LanguageCompetence>): SentenceAndTranslation? {
    val scheduled = repetitionService.getNextScheduledWord(learnLanguage, DateTime.now())
    if (scheduled != null) {
      logger.info("Repeating scheduled word: ${scheduled.lemma}, language: $learnLanguage")
      return sentenceAndWordsProvider.getEasiestRandomSentenceWith(scheduled, competence)
    }

    val attemptedWords = repetitionService.getAttemptedWords(learnLanguage)
    val canRepeatRandomWord = attemptedWords.any()
    val allWords = sentenceAndWordsProvider.getAllWords(learnLanguage)
    val canLearnNewWord = allWords.map { it.value }.minus(attemptedWords).any()

    if (canLearnNewWord == false && canRepeatRandomWord) throw Exception("Impossible state. Empty database?")
    if (canLearnNewWord && canRepeatRandomWord) {
      val nextTask = nextTaskDistribution.get(debugOptions.random)
      if (nextTask == NextTask.LearnNewWord) return learnNewWord(competence, allWords, attemptedWords)
      else return repeatRandomWord(attemptedWords, competence)
    }

    if (canLearnNewWord) return learnNewWord(competence, allWords, attemptedWords)
    else if (canRepeatRandomWord) return repeatRandomWord(attemptedWords, competence)
    else throw Exception("If this happens - then I am a steamship")
  }

  private fun repeatRandomWord(attemptedWords: List<Word>, competence: List<LanguageCompetence>): SentenceAndTranslation? {
    // TODO: user might want to NEVER see some words
    val word = attemptedWords.random(debugOptions.random)
    return sentenceAndWordsProvider.getEasiestRandomSentenceWith(word, competence)
  }

  private fun learnNewWord(competence: List<LanguageCompetence>, allWords: List<WithLevel<Word>>, attemptedWords: List<Word>): SentenceAndTranslation? {
    val attemptedWordsSet = attemptedWords.toHashSet()
    val word = allWords.filter { !attemptedWordsSet.contains(it.value) }.sortedBy { it.level }.first().value
    return sentenceAndWordsProvider.getEasiestRandomSentenceWith(word, competence)
  }
}