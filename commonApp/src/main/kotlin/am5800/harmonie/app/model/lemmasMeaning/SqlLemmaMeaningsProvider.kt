package am5800.harmonie.app.model.lemmasMeaning

import am5800.common.Lemma

class SqlLemmaMeaningsProvider : LemmaMeaningsProvider {
  override fun getMeanings(lemma: Lemma): List<String> {
    return emptyList()
  }
}