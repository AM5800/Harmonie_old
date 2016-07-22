package am5800.harmonie.app.model.features.parallelSentence

import am5800.common.Language
import am5800.common.Sentence
import am5800.common.WithLevel
import am5800.common.Word
import am5800.common.utils.EnumerableDistribution
import am5800.common.utils.functions.random
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.repetition.WordsRepetitionService
import am5800.harmonie.app.model.services.flow.LanguageCompetence
import am5800.harmonie.app.model.services.logging.LoggerProvider
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndTranslation
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndWordsProvider
import org.joda.time.DateTime

class ParallelSentenceSelectorImpl(private val repetitionService: WordsRepetitionService,
                                   private val debugOptions: DebugOptions,
                                   loggerProvider: LoggerProvider,
                                   private val sentenceAndWordsProvider: SentenceAndWordsProvider,
                                   private val sentenceScoreStorage: SentenceScoreStorage,
                                   private val sentenceSelectionStrategy: SentenceSelectionStrategy) : ParallelSentenceSelector {
  override fun submitScore(sentence: Sentence, score: SentenceScore) {
    sentenceScoreStorage.setScore(sentence, score)
  }

  private enum class NextTask {
    RepeatRandomWord, LearnNewWord
  }

  private val nextTaskDistribution = EnumerableDistribution.define<NextTask> {
    add(NextTask.LearnNewWord, 0.7)
    addRest(NextTask.RepeatRandomWord)
  }

  private val logger = loggerProvider.getLogger(javaClass)

  override fun selectSentenceToShow(learnLanguage: Language, languageCompetence: List<LanguageCompetence>): SentenceAndTranslation? {
    while (true) {
      val scheduled = repetitionService.getNextScheduledWord(learnLanguage, DateTime.now()) ?: break
      logger.info("Repeating scheduled word: ${scheduled.lemma}, language: $learnLanguage")
      val result = selectSentence(languageCompetence, scheduled)
      if (result != null) return result

      logger.info("Can't find sentence with word: ${scheduled.lemma}, language: $learnLanguage")
      repetitionService.remove(scheduled)
    }

    val attemptedWords = repetitionService.getAttemptedWords(learnLanguage)
    val canRepeatRandomWord = attemptedWords.any()
    val allWords = sentenceAndWordsProvider.getAllWords(learnLanguage)
    val canLearnNewWord = allWords.map { it.value }.minus(attemptedWords).any()

    if (canLearnNewWord == false && canRepeatRandomWord) throw Exception("Impossible state. Empty database?")
    if (canLearnNewWord && canRepeatRandomWord) {
      val nextTask = nextTaskDistribution.get(debugOptions.random)
      if (nextTask == NextTask.LearnNewWord) return learnNewWord(languageCompetence, allWords, attemptedWords)
      else return repeatRandomWord(attemptedWords, languageCompetence)
    }

    if (canLearnNewWord) return learnNewWord(languageCompetence, allWords, attemptedWords)
    else if (canRepeatRandomWord) return repeatRandomWord(attemptedWords, languageCompetence)
    else throw Exception("If this happens - then I am a steamship")
  }

  private fun repeatRandomWord(attemptedWords: List<Word>, competence: List<LanguageCompetence>): SentenceAndTranslation? {
    // TODO: user might want to NEVER see some words
    val word = attemptedWords.random(debugOptions.random)
    return selectSentence(competence, word)
  }

  private fun learnNewWord(competence: List<LanguageCompetence>, allWords: List<WithLevel<Word>>, attemptedWords: List<Word>): SentenceAndTranslation? {
    val attemptedWordsSet = attemptedWords.toHashSet()
    val word = allWords.filter { !attemptedWordsSet.contains(it.value) }.sortedBy { it.level }.first().value
    return selectSentence(competence, word)
  }

  private fun selectSentence(languageCompetence: List<LanguageCompetence>, scheduled: Word): SentenceAndTranslation? {
    val sentencesAndTranslation = sentenceAndWordsProvider.getEasiestSentencesWith(scheduled, languageCompetence, 50)
    val sentences = sentencesAndTranslation.map { it.sentence }
    val scores = sentenceScoreStorage.getScores(sentences)
    val result = sentenceSelectionStrategy.select(scores) ?: return null

    return sentencesAndTranslation[sentences.indexOf(result)]
  }
}