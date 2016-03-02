package am5800.harmonie.model.dbAccess

import am5800.common.Language
import am5800.common.db.DbSentence

data class DbWord(val id: Long, val lang: Language, val word: String)

interface WordsProvider {
  fun getWordsInSentence(sentence: DbSentence): List<DbWord>
}