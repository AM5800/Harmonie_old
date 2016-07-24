package am5800.harmonie.app.model.features.parallelSentence

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.Sentence
import am5800.common.utils.EnumerableDistribution
import am5800.common.utils.functions.random
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.repetition.LemmaRepetitionService
import am5800.harmonie.app.model.services.flow.LanguageCompetence
import am5800.harmonie.app.model.services.logging.LoggerProvider
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndLemmasProvider
import am5800.harmonie.app.model.services.sentencesAndWords.SentenceAndTranslation
import org.joda.time.DateTime

class ParallelSentenceSelectorImpl(private val repetitionService: LemmaRepetitionService,
                                   private val debugOptions: DebugOptions,
                                   loggerProvider: LoggerProvider,
                                   private val sentenceAndLemmasProvider: SentenceAndLemmasProvider,
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
      val scheduled = repetitionService.getNextScheduledLemma(learnLanguage, DateTime.now()) ?: break
      logger.info("Repeating scheduled lemma: ${scheduled.lemma}, language: $learnLanguage")
      val result = selectSentence(languageCompetence, scheduled)
      if (result != null) return result

      logger.info("Can't find sentence with lemma: ${scheduled.lemma}, language: $learnLanguage")
      repetitionService.remove(scheduled)
    }

    val attemptedWords = repetitionService.getAttemptedLemmas(learnLanguage)
    val canRepeatRandomWord = attemptedWords.any()
    val allWords = sentenceAndLemmasProvider.getAllLemmas(learnLanguage)
    val canLearnNewWord = allWords.map { it }.minus(attemptedWords).any()

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

  private fun repeatRandomWord(attemptedWords: List<Lemma>, competence: List<LanguageCompetence>): SentenceAndTranslation? {
    // TODO: user might want to NEVER see some words
    val word = attemptedWords.random(debugOptions.random)
    return selectSentence(competence, word)
  }

  private fun learnNewWord(competence: List<LanguageCompetence>, allLemmas: List<Lemma>, attemptedLemmas: List<Lemma>): SentenceAndTranslation? {
    val attemptedLemmasSet = attemptedLemmas.toHashSet()
    val word = allLemmas.filter { !attemptedLemmasSet.contains(it) }.sortedBy { it.difficultyLevel }.first()
    return selectSentence(competence, word)
  }

  private fun selectSentence(languageCompetence: List<LanguageCompetence>, scheduled: Lemma): SentenceAndTranslation? {
    val sentencesAndTranslation = sentenceAndLemmasProvider.getEasiestSentencesWith(scheduled, languageCompetence, 50)
    val sentences = sentencesAndTranslation.map { it.sentence }
    val scores = sentenceScoreStorage.getScores(sentences)
    val result = sentenceSelectionStrategy.select(scores) ?: return null

    return sentencesAndTranslation[sentences.indexOf(result)]
  }
}