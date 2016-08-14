package am5800.harmonie.app.model.parallelSentence

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.Sentence
import am5800.common.utils.EnumerableDistribution
import am5800.common.utils.functions.random
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.flow.LemmasOrderer
import am5800.harmonie.app.model.languageCompetence.LanguageCompetence
import am5800.harmonie.app.model.repetition.LemmaRepetitionService
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndTranslation
import org.joda.time.DateTime

class ParallelSentenceSelectorImpl(private val repetitionService: LemmaRepetitionService,
                                   private val debugOptions: DebugOptions,
                                   private val sentenceAndLemmasProvider: SentenceAndLemmasProvider,
                                   private val sentenceScoreStorage: SentenceScoreStorage,
                                   private val sentenceSelectionStrategy: SentenceSelectionStrategy,
                                   private val lemmasOrderer: LemmasOrderer) : ParallelSentenceSelector {
  override fun submitScore(sentence: Sentence, score: SentenceScore) {
    sentenceScoreStorage.setScore(sentence, score)
  }

  private enum class NextTask {
    LearnNewLemma, Repeat, RepeatRandom
  }

  private val nextTaskDistribution = EnumerableDistribution.define<NextTask> {
    add(NextTask.Repeat, 0.7)
    addRest(NextTask.LearnNewLemma)
  }

  override fun selectSentenceToShow(learnLanguage: Language, languageCompetence: List<LanguageCompetence>): SentenceAndTranslation? {
    val now = DateTime.now()
    val scheduledLemma = repetitionService.getNextScheduledLemma(learnLanguage, now)

    val attemptedLemmas = repetitionService.getAttemptedLemmas(learnLanguage)
    val allLemmas = sentenceAndLemmasProvider.getAllLemmasSorted(learnLanguage)

    val canLearnNewLemma = allLemmas.map { it }.minus(attemptedLemmas).any()
    val canRepeatRandomLemma = attemptedLemmas.any()
    val canRepeatScheduledLemma = scheduledLemma != null

    val task =
        if (canRepeatScheduledLemma && canLearnNewLemma) nextTaskDistribution.get(debugOptions.random)
        else if (!canRepeatScheduledLemma && canLearnNewLemma) NextTask.LearnNewLemma
        else if (!canRepeatScheduledLemma && !canLearnNewLemma && canRepeatRandomLemma) NextTask.RepeatRandom
        else throw Exception("Unsupported state")


    when (task) {
      NextTask.LearnNewLemma -> return learnNewLemma(languageCompetence, allLemmas, attemptedLemmas)
      NextTask.Repeat -> return selectSentence(languageCompetence, scheduledLemma!!)
      NextTask.RepeatRandom -> return repeatRandomLemma(attemptedLemmas, languageCompetence)
    }
  }

  private fun repeatRandomLemma(attemptedLemmas: List<Lemma>, competence: List<LanguageCompetence>): SentenceAndTranslation? {
    val lemma = attemptedLemmas.random(debugOptions.random)
    return selectSentence(competence, lemma)
  }

  private fun learnNewLemma(competence: List<LanguageCompetence>, allLemmas: List<Lemma>, attemptedLemmas: List<Lemma>): SentenceAndTranslation? {
    val attemptedLemmasSet = attemptedLemmas.toHashSet()
    val lemma = lemmasOrderer.reorder(allLemmas.filter { !attemptedLemmasSet.contains(it) }).first()
    return selectSentence(competence, lemma)
  }

  private fun selectSentence(languageCompetence: List<LanguageCompetence>, lemma: Lemma): SentenceAndTranslation? {
    val sentencesAndTranslation = sentenceAndLemmasProvider.getEasiestSentencesWith(lemma, languageCompetence, 50)
    val sentences = sentencesAndTranslation.map { it.sentence }
    val scores = sentenceScoreStorage.getScores(sentences)
    val result = sentenceSelectionStrategy.select(scores) ?: return null

    return sentencesAndTranslation[sentences.indexOf(result)]
  }
}