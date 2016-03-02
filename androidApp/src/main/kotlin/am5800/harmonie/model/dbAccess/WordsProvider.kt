package am5800.harmonie.model.dbAccess

import am5800.common.Language

data class DbWord(val id: Long, val lang: Language, val word: String)

interface WordsProvider {
  fun getWordsInSentence(sentence: DbSentence): List<DbWord>
}