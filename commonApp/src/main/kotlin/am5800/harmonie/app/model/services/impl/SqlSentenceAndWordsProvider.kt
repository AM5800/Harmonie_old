package am5800.harmonie.app.model.services.impl

import am5800.common.*
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.services.*

class SqlSentenceAndWordsProvider(private val contentDb: ContentDb, private val debugOptions: DebugOptions) : SentenceAndWordsProvider {
  override fun getRandomSentenceWith(word: Word, knownLanguage: Language, availableSentences: List<Sentence>): SentencePair? {
    val sentenceIds = availableSentences
        .map { it as SqlSentence }
        .map { it.id }
        .joinToString(", ")
    val wordId = (word as SqlWord).id
    val learnLanguage = word.language


    val query = """
        SELECT s1.id, s1.text, s2.id, s2.text FROM sentenceMapping
          INNER JOIN sentences AS s1
            ON s1.id = sentenceMapping.key
          INNER JOIN sentences AS s2
            ON s2.id = sentenceMapping.value
          INNER JOIN wordOccurrences
            ON wordOccurrences.sentenceId = s1.id
          WHERE s1.id IN ($sentenceIds)  AND wordId = $wordId AND s2.language='${knownLanguage.code}'
          ORDER BY RANDOM()
          LIMIT 1
    """

    val result = contentDb.query4<Long, String, Long, String>(query).singleOrNull() ?: return null

    val knownLanguageSentence = SqlSentence(result.value3, knownLanguage, result.value4)
    val learnLanguageSentence = SqlSentence(result.value1, learnLanguage, result.value2)
    return SentencePair(knownLanguageSentence, learnLanguageSentence)
  }

  override fun getOccurrences(sentence: Sentence): List<WordOccurrence> {
    if (sentence !is SqlSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val query = """
        SELECT words.id, words.lemma, wordOccurrences.startIndex, wordOccurrences.endIndex, words.language
        FROM words
          INNER JOIN wordOccurrences
            ON words.id = wordOccurrences.wordId
        WHERE wordOccurrences.sentenceId = ${sentence.id}
    """

    val wordsData = contentDb.query5<Long, String, Int, Int, String>(query)

    val result = wordsData.map {
      WordOccurrence(SqlWord(it.value1, LanguageParser.parse(it.value5), it.value2), sentence, it.value3, it.value4)
    }

    if (result.isNotEmpty() && result.any { it.word.language != sentence.language }) throw Exception("Word language differ from sentence language")
    return result
  }

  override fun getWordsInSentence(sentence: Sentence): List<Word> {
    if (sentence !is SqlSentence) throw Exception("Unsupported type: " + sentence.javaClass.name)

    val sentenceId = sentence.id
    val query = "SELECT id, lemma FROM words WHERE id IN (SELECT wordId FROM wordOccurrences WHERE sentenceId = $sentenceId)"

    val result = contentDb.query2<Long, String>(query)

    return result.map { SqlWord(it.first, sentence.language, it.second) }
  }
}