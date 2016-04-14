package am5800.harmonie.app.model.services.impl

import am5800.common.*
import am5800.harmonie.app.model.services.*

class SqlSentenceProvider(private val contentDb: ContentDb) : SentenceProvider {
  override fun getAvailableLanguagePairs(): Collection<WithCounter<LanguagePair>> {
    return contentDb.query3<String, String, Long>("SELECT knownLanguage, learnLanguage, count FROM sentenceLanguages")
        .map { WithCounter(LanguagePair(LanguageParser.parse(it.value1), LanguageParser.parse(it.value2)), it.value3.toInt()) }
  }

  override fun getOccurrences(sentence: Sentence): List<WordOccurrence> {
    if (sentence !is SqlSentence) throw Exception("Only SqlSentences supported")

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

  override fun getRandomSentencePair(learnLanguage: Language, knownLanguage: Language): SentencePair? {
    val learnLang = learnLanguage.code
    val knownLang = knownLanguage.code
    val query = """
        SELECT s1.id, s1.text, s2.id, s2.text
          FROM sentenceMapping
          INNER JOIN sentences AS s1
            ON s1.id = sentenceMapping.key
          INNER JOIN sentences AS s2
            ON s2.id = sentenceMapping.value
          WHERE s1.language='$learnLang' AND s2.language='$knownLang'
          ORDER BY RANDOM()
          LIMIT 1
    """

    return contentDb.query4<Long, String, Long, String>(query)
        .map { SentencePair(SqlSentence(it.value3, knownLanguage, it.value4), SqlSentence(it.value1, learnLanguage, it.value2)) }
        .singleOrNull()
  }
}