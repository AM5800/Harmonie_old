package testUtils

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.LemmaOccurrence
import am5800.common.Sentence
import am5800.harmonie.app.model.languageCompetence.LanguageCompetence
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndTranslation

class SentenceAndLemmasProviderMock : SentenceAndLemmasProvider {

  val lemmas = mutableListOf<Lemma>()

  override fun getOccurrences(sentence: Sentence): List<LemmaOccurrence> {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getEasiestSentencesWith(lemma: Lemma, competence: List<LanguageCompetence>, amount: Int): List<SentenceAndTranslation> {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getAllLemmasSorted(learnLanguage: Language): List<Lemma> {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getLemmasByIds(lemmaIds: List<String>): List<Lemma> {
    val idsSet = lemmaIds.toSet()
    return lemmas.filter { idsSet.contains(it.id) }
  }

}