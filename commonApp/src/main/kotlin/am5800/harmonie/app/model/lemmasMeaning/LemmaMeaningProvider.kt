package am5800.harmonie.app.model.lemmasMeaning

import am5800.common.Language
import am5800.common.Lemma


interface LemmaTranslationsProvider {
  fun getTranslations(lemma: Lemma, translationsLanguage: Language): List<String>
}