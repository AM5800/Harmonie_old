package am5800.harmonie.app.model.dbAccess

import am5800.common.Language
import am5800.common.db.DbSentence
import am5800.common.db.DbWord

interface WordsProvider {
  fun getWordsInSentence(sentence: DbSentence): List<DbWord>
  fun tryFindWord(word: String, language: Language): DbWord?
}