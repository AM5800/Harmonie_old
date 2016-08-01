package am5800.harmonie.app.model.lemmasMeaning

import am5800.common.Language
import am5800.common.Lemma


interface LemmaMeaningsProvider {
  fun getMeaningsAsSingleString(lemma: Lemma, meaningsLanguage: Language): String?
}