package am5800.harmonie.model.dbAccess

import am5800.common.Language

interface SentenceProvider {
  fun getSentences(languageFrom: Language, languageTo: Language): List<Pair<DbSentence, DbSentence>>
}