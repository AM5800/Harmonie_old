package am5800.harmonie.app.model.parallelSentence

import am5800.common.Language
import am5800.common.Sentence
import am5800.harmonie.app.model.flow.LanguageCompetence
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndTranslation

enum class SentenceScore {
  TotalBlackout, Unclear, Uncertain, Clear
}

interface ParallelSentenceSelector {
  fun selectSentenceToShow(learnLanguage: Language, languageCompetence: List<LanguageCompetence>): SentenceAndTranslation?
  fun submitScore(sentence: Sentence, score: SentenceScore)
}