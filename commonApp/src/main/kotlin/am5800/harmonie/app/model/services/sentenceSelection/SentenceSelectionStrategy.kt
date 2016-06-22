package am5800.harmonie.app.model.services.sentenceSelection

import am5800.common.Language
import am5800.common.utils.functions.random
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.repetition.WordsRepetitionService
import am5800.harmonie.app.model.services.EnumerableDistribution
import am5800.harmonie.app.model.services.SentenceAndWordsProvider
import am5800.harmonie.app.model.services.SentencePair
import am5800.harmonie.app.model.services.SentenceSelector
import am5800.harmonie.app.model.services.learnGraph.LearnGraphService
import am5800.harmonie.app.model.services.logging.LoggerProvider
import org.joda.time.DateTime

class SentenceSelectionStrategy(private val repetitionService: WordsRepetitionService,
                                private val debugOptions: DebugOptions,
                                loggerProvider: LoggerProvider,
                                private val sentenceAndWordsProvider: SentenceAndWordsProvider,
                                private val learnGraphService: LearnGraphService) : SentenceSelector {

  private enum class NextTask {
    RepeatRandomWord, LearnNewWord
  }

  private val nextTaskDistribution = EnumerableDistribution.define<NextTask> {
    add(NextTask.LearnNewWord, 0.7)
    addRest(NextTask.LearnNewWord)
  }

  private val logger = loggerProvider.getLogger(javaClass)

  override fun findBestSentenceByAttempts(learnLanguage: Language, knownLanguage: Language): SentencePair? {
    val scheduled = repetitionService.getNextScheduledWord(learnLanguage, DateTime.now())
    if (scheduled != null) {
      logger.info("Repeating scheduled word: ${scheduled.lemma}, language: $learnLanguage")
      return sentenceAndWordsProvider.getRandomSentenceWith(scheduled, knownLanguage, learnGraphService.getUnlockedSentences(learnLanguage))
    }

    val canRepeatRandomWord = repetitionService.getAttemptedWords(learnLanguage).any()
    val canLearnNewWord = learnGraphService.canUnlockNextWord() || hasNotAttemptedWords(learnLanguage)

    if (canLearnNewWord == false && canRepeatRandomWord) throw Exception("Impossible state. Empty database?")
    if (canLearnNewWord && canRepeatRandomWord) {
      val nextTask = nextTaskDistribution.get(debugOptions.random)
      if (nextTask == NextTask.LearnNewWord) return learnNewWord(learnLanguage, knownLanguage)
      else return repeatRandomWord(learnLanguage, knownLanguage)
    }

    if (canLearnNewWord) return learnNewWord(learnLanguage, knownLanguage)
    else if (canRepeatRandomWord) return repeatRandomWord(learnLanguage, knownLanguage)
    else throw Exception("If this happens - then I am a steamship")
  }

  private fun hasNotAttemptedWords(learnLanguage: Language): Boolean {
    val attemptedWords = repetitionService.getAttemptedWords(learnLanguage)
    val unlockedWords = learnGraphService.getUnlockedWords(learnLanguage)
    val notAttemptedWords = unlockedWords.minus(attemptedWords)
    return notAttemptedWords.any()
  }

  private fun repeatRandomWord(learnLanguage: Language, knownLanguage: Language): SentencePair? {
    // TODO: user might want to NEVER see some words
    val word = learnGraphService.getUnlockedWords(learnLanguage).random(debugOptions.random)
    val sentences = learnGraphService.getUnlockedSentences(learnLanguage)
    return sentenceAndWordsProvider.getRandomSentenceWith(word, knownLanguage, sentences)
  }

  private fun learnNewWord(learnLanguage: Language, knownLanguage: Language): SentencePair? {
    val attemptedWords = repetitionService.getAttemptedWords(learnLanguage)
    val unlockedWords = learnGraphService.getUnlockedWords(learnLanguage)
    val notAttemptedWords = unlockedWords.minus(attemptedWords)
    val word =
        if (notAttemptedWords.size == 0)
          learnGraphService.unlockNextWordsGroup().first()
        else notAttemptedWords.first()

    val sentences = learnGraphService.getUnlockedSentences(learnLanguage)
    return sentenceAndWordsProvider.getRandomSentenceWith(word, knownLanguage, sentences)
  }
}