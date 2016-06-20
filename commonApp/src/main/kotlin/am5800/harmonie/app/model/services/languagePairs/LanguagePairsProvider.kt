package am5800.harmonie.app.model.services.languagePairs

import am5800.common.LanguagePair
import am5800.common.WithCounter

interface LanguagePairsProvider {
  fun getAvailableLanguagePairs(): Collection<WithCounter<LanguagePair>>
}