package am5800.harmonie.app.model.dbAccess

import am5800.common.Language
import am5800.common.db.Sentence

interface SentenceSelector {
  fun findBestSentence(languageFrom: Language, languageTo: Language): Pair<Sentence, Sentence>?
}