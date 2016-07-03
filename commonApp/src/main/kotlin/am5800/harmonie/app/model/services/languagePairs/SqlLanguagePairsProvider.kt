package am5800.harmonie.app.model.services.languagePairs

import am5800.common.LanguagePair
import am5800.common.LanguageParser
import am5800.common.WithCounter
import am5800.harmonie.app.model.services.ContentDb
import am5800.harmonie.app.model.services.query3


class SqlLanguagePairsProvider(private val contentDb: ContentDb) : LanguagePairsProvider {
  override fun getAvailableLanguagePairs(): Collection<WithCounter<LanguagePair>> {
    return contentDb.query3<String, String, Long>("SELECT knownLanguage, learnLanguage, count FROM sentenceLanguages")
        .map { WithCounter(LanguagePair(LanguageParser.parse(it.value1), LanguageParser.parse(it.value2)), it.value3.toInt()) }
  }
}