package am5800.harmonie.app.model.parallelSentence

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.Sentence
import am5800.common.utils.EnumerableDistribution
import am5800.common.utils.functions.random
import am5800.common.utils.functions.randomOrNull
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
    val repetitionQueueSize = repetitionService.countOnDueLemmas(learnLanguage, now)

    val attemptedLemmas = repetitionService.getAttemptedLemmas(learnLanguage)
    val allLemmas = sentenceAndLemmasProvider.getAllLemmasSorted(learnLanguage)

    val canLearnNewLemma = allLemmas.map { it }.minus(attemptedLemmas).any()
    val canRepeatRandomLemma = attemptedLemmas.any()
    val canRepeatScheduledLemma = scheduledLemma != null
    val avoidLearningNew = repetitionQueueSize > 20

    val task =
        if (canRepeatScheduledLemma && canLearnNewLemma && !avoidLearningNew) nextTaskDistribution.get(debugOptions.random)
        else if (!canRepeatScheduledLemma && canLearnNewLemma && !avoidLearningNew) NextTask.LearnNewLemma
        else if (avoidLearningNew && canRepeatScheduledLemma) NextTask.Repeat
        else if (!canRepeatScheduledLemma && !canLearnNewLemma && canRepeatRandomLemma) NextTask.RepeatRandom
        else throw Exception("Unsupported state: $canLearnNewLemma, $canRepeatRandomLemma, $canRepeatScheduledLemma, $avoidLearningNew")

    when (task) {
      NextTask.LearnNewLemma -> return learnNewLemma(languageCompetence, allLemmas, attemptedLemmas)
      NextTask.Repeat -> return selectSentence(languageCompetence, scheduledLemma!!, avoidLearningNew)
      NextTask.RepeatRandom -> return repeatRandomLemma(attemptedLemmas, languageCompetence, avoidLearningNew)
    }
  }

  private fun repeatRandomLemma(attemptedLemmas: List<Lemma>, competence: List<LanguageCompetence>, avoidLearningNew: Boolean): SentenceAndTranslation? {
    val lemma = attemptedLemmas.random(debugOptions.random)
    return selectSentence(competence, lemma, avoidLearningNew)
  }

  private fun learnNewLemma(competence: List<LanguageCompetence>, allLemmas: List<Lemma>, attemptedLemmas: List<Lemma>): SentenceAndTranslation? {
    val attemptedLemmasSet = attemptedLemmas.toHashSet()
    val lemma = lemmasOrderer.reorder(allLemmas.filter { !attemptedLemmasSet.contains(it) }).first()
    return selectSentence(competence, lemma, false)
  }

  private fun selectSentence(languageCompetence: List<LanguageCompetence>, lemma: Lemma, avoidLearningNew: Boolean): SentenceAndTranslation? {
    val sentencesAndTranslation = sentenceAndLemmasProvider.getEasiestSentencesWith(lemma, languageCompetence, 50)
    val sentences = sentencesAndTranslation.map { it.sentence }
    val scores = sentenceScoreStorage.getScores(sentences)
    val result = select(scores, avoidLearningNew) ?: return null

    return sentencesAndTranslation[sentences.indexOf(result)]
  }

  private fun select(sentences: List<Pair<Sentence, SentenceScore?>>, avoidLearningNew: Boolean): Sentence? {
    if (sentences.isEmpty()) return null

    if (avoidLearningNew) {
      val wellKnownSentences = sentences
          .filter { it.second == SentenceScore.Clear || it.second == SentenceScore.Uncertain }
          .map { it.first }
      val wellKnownSentence = wellKnownSentences.randomOrNull(debugOptions.random)
      if (wellKnownSentence != null) return wellKnownSentence
    }

    val firstUnknown = sentences.firstOrNull { it.second == null }
    if (firstUnknown != null) return firstUnknown.first

    val result = sentences.filter { it.second != SentenceScore.Unclear }.randomOrNull(debugOptions.random) ?: return sentences.first().first

    return result.first
  }
}