package am5800.harmonie.app.model.lemmasMeaning

import am5800.common.Lemma


interface LemmaMeaningsProvider {
  fun getMeanings(lemma: Lemma): List<String>
}