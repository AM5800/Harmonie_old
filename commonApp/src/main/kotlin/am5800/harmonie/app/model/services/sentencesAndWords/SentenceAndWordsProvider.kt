package am5800.harmonie.app.model.services.sentencesAndWords

import am5800.common.*
import am5800.harmonie.app.model.features.flow.LanguageCompetence

data class SentenceAndTranslation(val sentence: Sentence, val translation: Sentence)

interface SentenceAndWordsProvider {
  fun getWordsInSentence(sentence: Sentence): List<Word>
  fun getOccurrences(sentence: Sentence): List<WordOccurrence>

  fun getEasiestRandomSentenceWith(word: Word, competence: List<LanguageCompetence>): SentenceAndTranslation?

  fun getAllWords(learnLanguage: Language): List<WithLevel<Word>>
}