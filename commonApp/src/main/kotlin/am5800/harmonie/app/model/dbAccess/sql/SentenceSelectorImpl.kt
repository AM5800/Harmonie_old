package am5800.harmonie.app.model.dbAccess.sql

import am5800.common.Language
import am5800.common.code
import am5800.common.db.ContentDbConstants
import am5800.common.db.Sentence
import am5800.common.utils.functions.shuffle
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.dbAccess.SentenceSelector
import am5800.harmonie.app.model.dbAccess.WordsRepetitionService
import am5800.harmonie.app.model.logging.LoggerProvider
import org.joda.time.DateTime


class SentenceSelectorImpl(private val repetitionService: WordsRepetitionService,
                           loggerProvider: LoggerProvider,
                           private val debugOptions: DebugOptions) : SentenceSelector, ContentDbConsumer {
  private val logger = loggerProvider.getLogger(javaClass)

  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {

  }

  private var database: ContentDb? = null

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  override fun findBestSentence(languageFrom: Language, languageTo: Language): Pair<Sentence, Sentence>? {

    val attempted = repetitionService.getAttemptedWords(languageFrom).filterIsInstance<SqlWord>()
    val scheduled = repetitionService.getScheduledWords(languageFrom, DateTime.now()).filterIsInstance<SqlWord>()

    logger.info("Looking for best sentence. ${scheduled.size} words scheduled")

    if (!scheduled.isEmpty()) return findBestSentence(languageFrom, languageTo, scheduled)

    val nextByFrequencyWord = findNextByFrequencyWord(attempted, languageFrom)
    logger.info("Next by frequency word is: ${nextByFrequencyWord?.lemma}")

    if (nextByFrequencyWord == null) return getRandomSentence(languageFrom, languageTo)

    return findBestSentence(languageFrom, languageTo, listOf(nextByFrequencyWord))
  }

  private fun getRandomSentence(languageFrom: Language, languageTo: Language): Pair<Sentence, Sentence>? {
    val translations = ContentDbConstants.sentenceTranslationsTableName
    val sentences = ContentDbConstants.sentencesTableName
    val langFrom = languageFrom.code()
    val langTo = languageTo.code()
    val query = """
        SELECT s1.id, s1.text, s2.id, s2.text
          FROM $translations
          INNER JOIN $sentences AS s1
            ON s1.id = $translations.key
          INNER JOIN $sentences AS s2
            ON s2.id = $translations.value
          WHERE s1.language='$langFrom' AND s2.language='$langTo'
          ORDER BY RANDOM()
          LIMIT 1
    """

    return database!!.query4<Long, String, Long, String>(query)
        .map { Pair(SqlSentence(it.value1, languageFrom, it.value2), SqlSentence(it.value3, languageTo, it.value4)) }
        .singleOrNull()
  }

  private fun findBestSentence(languageFrom: Language, languageTo: Language, contianingWords: List<SqlWord>): Pair<Sentence, Sentence> {
    if (contianingWords.isEmpty()) throw Exception("Nothing to search for")
    val db = database!!
    val translations = ContentDbConstants.sentenceTranslationsTableName
    val sentences = ContentDbConstants.sentencesTableName
    val langFrom = languageFrom.code()
    val langTo = languageTo.code()
    val difficulties = ContentDbConstants.sentenceDifficultyTableName
    val wordOccurrences = ContentDbConstants.wordOccurrencesTableName

    val includeIds = contianingWords.map { it.id }.joinToString(", ")

    val searchQuery = """
        SELECT s1.id, s1.text, s2.id, s2.text, $difficulties.difficulty
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

    val queryResult = db.query5<Long, String, Long, String, Long>(searchQuery)

    if (queryResult.size >= 1) {
      val minDifficulty = queryResult.first().value5
      return queryResult.takeWhile { it.value5 == minDifficulty }
          .map { Pair(SqlSentence(it.value1, languageFrom, it.value2), SqlSentence(it.value3, languageTo, it.value4)) }
          .shuffle(debugOptions.randomSeed)
          .first()
    } else throw Exception("No sentences found")
  }

  private fun findNextByFrequencyWord(attempted: Collection<SqlWord>, language: Language): SqlWord? {
    val occurrences = ContentDbConstants.wordOccurrencesTableName
    val words = ContentDbConstants.wordsTableName
    val lang = language.code()
    val ids = attempted.map { it.id }.joinToString(", ")
    val query = """
      SELECT $words.id, $words.lemma
        FROM words
        INNER JOIN $occurrences
          ON $occurrences.wordId = $words.id
        WHERE $words.language='$lang' AND $words.id NOT IN ($ids)
        GROUP BY $words.id
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """
    return database!!.query2<Long, String>(query).map { SqlWord(it.first, language, it.second) }.singleOrNull()
  }
}