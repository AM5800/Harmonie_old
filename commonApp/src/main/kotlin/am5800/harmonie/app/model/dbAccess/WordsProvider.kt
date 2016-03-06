package am5800.harmonie.app.model.dbAccess

import am5800.common.Language
import am5800.common.db.Sentence
import am5800.common.db.Word

interface WordsProvider {
  fun getWordsInSentence(sentence: Sentence): List<Word>
  fun tryFindWord(word: String, language: Language): Word?
}