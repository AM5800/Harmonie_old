package am5800.harmonie.app.model.services.impl

import am5800.common.*
import am5800.common.db.ContentDbConstants
import am5800.harmonie.app.model.services.ContentDb
import am5800.harmonie.app.model.services.SentenceProvider
import am5800.harmonie.app.model.services.query2
import am5800.harmonie.app.model.services.query4

class SqlSentenceProvider(private val contentDb: ContentDb) : SentenceProvider {
  override fun getAvailableLanguagePairs(): Collection<LanguagePair> {
    return contentDb.query2<String, String>("SELECT knownLanguage, learnLanguage FROM sentenceLanguages")
        .map { LanguagePair(LanguageParser.parse(it.first), LanguageParser.parse(it.second)) }
  }

  override fun getOccurrences(sentence: Sentence): List<WordOccurrence> {
    if (sentence !is SqlSentence) throw Exception("Only SqlSentences supported")

    val words = ContentDbConstants.words
    val occurrences = ContentDbConstants.wordOccurrences

    val query = """
        SELECT $words.id, $words.lemma, $occurrences.startIndex, $occurrences.endIndex
        FROM $words
          INNER JOIN $occurrences
            ON $words.id = $occurrences.wordId
        WHERE $occurrences.sentenceId = ${sentence.id}
    """

    val wordsData = contentDb.query4<Long, String, Int, Int>(query)

    return wordsData.map {
      WordOccurrence(SqlWord(it.value1, sentence.language, it.value2), sentence, it.value3, it.value4)
    }
  }

  override fun getWordsInSentence(sentence: Sentence): List<Word> {
    if (sentence !is SqlSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val words = ContentDbConstants.words
    val occurrences = ContentDbConstants.wordOccurrences
    val sentenceId = sentence.id
    val query = "SELECT id, lemma FROM $words WHERE id IN (SELECT wordId FROM $occurrences WHERE sentenceId = $sentenceId)"

    val result = contentDb.query2<Long, String>(query)

    return result.map { SqlWord(it.first, sentence.language, it.second) }
  }
}