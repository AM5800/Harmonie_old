package am5800.harmonie.app.model.services.sentencesAndWords

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.LemmaOccurrence
import am5800.common.Sentence
import am5800.harmonie.app.model.services.flow.LanguageCompetence

data class SentenceAndTranslation(val sentence: Sentence, val translation: Sentence)

interface SentenceAndLemmasProvider {
  fun getOccurrences(sentence: Sentence): List<LemmaOccurrence>
  fun getEasiestSentencesWith(lemma: Lemma, competence: List<LanguageCompetence>, amount: Int): List<SentenceAndTranslation>
  fun getAllLemmas(learnLanguage: Language): List<Lemma>
  fun getLemmasByIds(lemmaId: List<String>): List<Lemma>
}