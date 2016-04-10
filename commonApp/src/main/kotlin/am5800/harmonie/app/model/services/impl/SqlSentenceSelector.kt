package am5800.harmonie.app.model.services.impl

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.db.ContentDbConstants
import am5800.common.utils.functions.shuffle
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.features.repetition.WordsRepetitionService
import am5800.harmonie.app.model.services.*
import am5800.harmonie.app.model.services.logging.LoggerProvider
import org.joda.time.DateTime

class SqlSentenceSelector(private val repetitionService: WordsRepetitionService,
                          loggerProvider: LoggerProvider,
                          private val contentDb: ContentDb,
                          private val debugOptions: DebugOptions,
                          private val wordSelector: WordSelector) : SentenceSelector {

  private val logger = loggerProvider.getLogger(javaClass)

  override fun findBestSentenceByAttempts(languageFrom: Language, languageTo: Language): SentenceSelectorResult? {
    val scheduled = repetitionService.getScheduledWords(languageFrom, DateTime.now()).filterIsInstance<SqlWord>()

    logger.info("Looking for best sentence. ${scheduled.size} words scheduled")

    if (!scheduled.isEmpty()) return findBestSentence(languageFrom, languageTo, scheduled)

    val nextWord = wordSelector.findBestWord(languageFrom) as? SqlWord
    logger.info("Next by frequency word is: ${nextWord?.lemma}")

    if (nextWord == null) return getRandomSentence(languageFrom, languageTo)

    return findBestSentence(languageFrom, languageTo, listOf(nextWord))
  }

  private fun getRandomSentence(languageFrom: Language, languageTo: Language): SentenceSelectorResult? {
    val translations = ContentDbConstants.sentenceTranslations
    val sentences = ContentDbConstants.sentences
    val langFrom = languageFrom.code
    val langTo = languageTo.code
    val query = """
        SELECT s1.id, s1.text, s2.id, s2.text, s2.language
          FROM $translations
          INNER JOIN $sentences AS s1
            ON s1.id = $translations.key
          INNER JOIN $sentences AS s2
            ON s2.id = $translations.value
          WHERE s1.language='$langFrom' AND s2.language='$langTo'
          ORDER BY RANDOM()
          LIMIT 1
    """

    return contentDb.query5<Long, String, Long, String, String>(query)
        .map { SentenceSelectorResult(SqlSentence(it.value1, languageFrom, it.value2), SqlSentence(it.value3, LanguageParser.Companion.parse(it.value5), it.value4), emptySet()) }
        .singleOrNull()
  }

  private fun findBestSentence(languageFrom: Language, languageTo: Language, containingWords: List<SqlWord>): SentenceSelectorResult? {
    if (containingWords.isEmpty()) throw Exception("Nothing to search for")
    val translations = ContentDbConstants.sentenceTranslations
    val sentences = ContentDbConstants.sentences
    val langFrom = languageFrom.code
    val langTo = languageTo.code
    val difficulties = ContentDbConstants.sentenceDifficulty
    val wordOccurrences = ContentDbConstants.wordOccurrences

    val includeIds = containingWords.map { it.id }.joinToString(", ")

    val searchQuery = """
        SELECT s1.id, s1.text, s2.id, s2.text, $difficulties.difficulty, s2.language
          FROM $translations
          INNER JOIN $sentences AS s1
            ON s1.id = $translations.key
          INNER JOIN $sentences AS s2
            ON s2.id = $translations.value
          INNER JOIN $difficulties
            ON s1.id = $difficulties.sentenceId
          INNER JOIN $wordOccurrences
            ON s1.id = $wordOccurrences.sentenceId
          WHERE s1.language='$langFrom' AND s2.language='$langTo' AND $wordOccurrences.wordId IN ($includeIds)
          GROUP BY s1.id
          ORDER BY $difficulties.difficulty
          LIMIT 20
		"""

    val queryResult = contentDb.query6<Long, String, Long, String, Long, String>(searchQuery)

    if (queryResult.size >= 1) {
      val minDifficulty = queryResult.first().value5
      return queryResult.takeWhile { it.value5 == minDifficulty }
          .map { SentenceSelectorResult(SqlSentence(it.value1, languageFrom, it.value2), SqlSentence(it.value3, LanguageParser.Companion.parse(it.value6), it.value4), containingWords.toSet()) }
          .shuffle(debugOptions.randomSeed)
          .first()
    } else throw Exception("No sentences found")
  }
}