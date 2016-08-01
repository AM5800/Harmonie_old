package am5800.harmonie.app.model.parallelSentence

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.Sentence
import am5800.common.utils.EnumerableDistribution
import am5800.common.utils.functions.random
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.flow.LemmasOrderer
import am5800.harmonie.app.model.languageCompetence.LanguageCompetence
import am5800.harmonie.app.model.logging.LoggerProvider
import am5800.harmonie.app.model.repetition.LemmaRepetitionService
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndTranslation
import org.joda.time.DateTime

class ParallelSentenceSelectorImpl(private val repetitionService: LemmaRepetitionService,
                                   private val debugOptions: DebugOptions,
                                   loggerProvider: LoggerProvider,
                                   private val sentenceAndLemmasProvider: SentenceAndLemmasProvider,
                                   private val sentenceScoreStorage: SentenceScoreStorage,
                                   private val sentenceSelectionStrategy: SentenceSelectionStrategy,
                                   private val lemmasOrderer: LemmasOrderer) : ParallelSentenceSelector {
  override fun submitScore(sentence: Sentence, score: SentenceScore) {
    sentenceScoreStorage.setScore(sentence, score)
  }

  private enum class NextTask {
    RepeatRandomLemma, LearnNewLemma
  }

  private val nextTaskDistribution = EnumerableDistribution.define<NextTask> {
    add(NextTask.LearnNewLemma, 0.7)
    addRest(NextTask.RepeatRandomLemma)
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

    val attemptedLemmas = repetitionService.getAttemptedLemmas(learnLanguage)
    val canRepeatRandomLemma = attemptedLemmas.any()
    val allLemmas = sentenceAndLemmasProvider.getAllLemmas(learnLanguage)
    val canLearnNewLemma = allLemmas.map { it }.minus(attemptedLemmas).any()

    if (canLearnNewLemma == false && canRepeatRandomLemma) throw Exception("Impossible state. Empty database?")
    if (canLearnNewLemma && canRepeatRandomLemma) {
      val nextTask = nextTaskDistribution.get(debugOptions.random)
      if (nextTask == NextTask.LearnNewLemma) return learnNewLemma(languageCompetence, allLemmas, attemptedLemmas)
      else return repeatRandomLemma(attemptedLemmas, languageCompetence)
    }

    if (canLearnNewLemma) return learnNewLemma(languageCompetence, allLemmas, attemptedLemmas)
    else if (canRepeatRandomLemma) return repeatRandomLemma(attemptedLemmas, languageCompetence)
    else throw Exception("If this happens - then I am a steamship")
  }

  private fun repeatRandomLemma(attemptedLemmas: List<Lemma>, competence: List<LanguageCompetence>): SentenceAndTranslation? {
    // TODO: user might want to NEVER see some words
    val lemma = attemptedLemmas.random(debugOptions.random)
    return selectSentence(competence, lemma)
  }

  private fun learnNewLemma(competence: List<LanguageCompetence>, allLemmas: List<Lemma>, attemptedLemmas: List<Lemma>): SentenceAndTranslation? {
    val attemptedLemmasSet = attemptedLemmas.toHashSet()
    val lemma = lemmasOrderer.reorder(allLemmas.filter { !attemptedLemmasSet.contains(it) }).first()
    return selectSentence(competence, lemma)
  }

  private fun selectSentence(languageCompetence: List<LanguageCompetence>, scheduled: Lemma): SentenceAndTranslation? {
    val sentencesAndTranslation = sentenceAndLemmasProvider.getEasiestSentencesWith(scheduled, languageCompetence, 50)
    val sentences = sentencesAndTranslation.map { it.sentence }
    val scores = sentenceScoreStorage.getScores(sentences)
    val result = sentenceSelectionStrategy.select(scores) ?: return null

    return sentencesAndTranslation[sentences.indexOf(result)]
  }
}