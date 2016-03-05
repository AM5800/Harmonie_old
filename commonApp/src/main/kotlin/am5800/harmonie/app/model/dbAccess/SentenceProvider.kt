package am5800.harmonie.app.model.dbAccess

import am5800.common.Language
import am5800.common.db.DbSentence
import am5800.common.db.DbWord

interface SentenceProvider {
  fun getSentences(languageFrom: Language, languageTo: Language): List<Pair<DbSentence, DbSentence>>
  fun getSentencesWithAnyOfWords(languageFrom: Language, languageTo: Language, words: List<DbWord>): List<Pair<DbSentence, DbSentence>>
}