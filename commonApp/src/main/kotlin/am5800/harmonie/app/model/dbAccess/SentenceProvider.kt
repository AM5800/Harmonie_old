package am5800.harmonie.app.model.dbAccess

import am5800.common.Language
import am5800.common.db.Sentence
import am5800.common.db.Word

interface SentenceProvider {
  fun getSentences(languageFrom: Language, languageTo: Language): List<Pair<Sentence, Sentence>>
  fun getSentencesWithAnyOfWords(languageFrom: Language, languageTo: Language, words: List<Word>): List<Pair<Sentence, Sentence>>
}