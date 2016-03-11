package am5800.harmonie.app.model.dbAccess.sql

import am5800.common.Language
import am5800.common.LanguageParser
import am5800.common.db.ContentDbConstants
import am5800.common.db.Sentence
import am5800.common.utils.functions.shuffle
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.dbAccess.RepetitionService
import am5800.harmonie.app.model.dbAccess.SentenceProvider
import am5800.harmonie.app.model.dbAccess.SentenceSelector
import am5800.harmonie.app.model.logging.LoggerProvider
import org.joda.time.DateTime


class SentenceSelectorImpl(private val repetitionService: RepetitionService,
                           private val sentenceProvider: SentenceProvider,
                           private val debugOptions: DebugOptions, loggerProvider: LoggerProvider) : SentenceSelector, ContentDbConsumer {
  private val logger = loggerProvider.getLogger(javaClass)

  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {

  }

  private var database: ContentDb? = null

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  override fun findBestSentence(languageFrom: Language, languageTo: Language, attemptCategory: String): Pair<Sentence, Sentence> {

    val attempted = getAttemptedWords(attemptCategory).toSet()
    val scheduled = getScheduledWords(attemptCategory, attempted)

    logger.info("Looking for best sentence. ${attempted.size} words attempted, ${scheduled.size} words scheduled")

    if (!scheduled.isEmpty()) return findBestSentence(languageFrom, languageTo, scheduled, attempted)

    val nextByFrequencyWord = findNextByFrequencyWord(attempted)
    logger.info("Next by frequency word is: ${nextByFrequencyWord?.lemma}")

    return findBestSentence(languageFrom, languageTo, listOf(nextByFrequencyWord).filterNotNull(), attempted)
  }

  private fun findBestSentence(languageFrom: Language, languageTo: Language, contianingWords: List<SqlWord>, attempted: Set<SqlWord>): Pair<Sentence, Sentence> {
    val db = database!!
    val map = ContentDbConstants.sentenceTranslationsTableName
    val sentences = ContentDbConstants.sentencesTableName
    val langFrom = LanguageParser.toShortString(languageFrom)
    val langTo = LanguageParser.toShortString(languageTo)

    var query = """
        SELECT s1.id, s1.text, s2.id, s2.text
        FROM $map
          INNER JOIN $sentences AS s1
            ON $map.key = s1.id
          INNER JOIN $sentences AS s2
            ON $map.value = s2.id
        WHERE s1.language = '$langFrom' AND s2.language ='$langTo'"""

    if (contianingWords.any()) {
      val ids = contianingWords.map { it.id }.joinToString(", ")
      val occurrences = ContentDbConstants.wordOccurrencesTableName
      query += " AND s1.id IN (SELECT sentenceId FROM $occurrences WHERE wordId IN ($ids))"
    }

    val result = db.query4<Long, String, Long, String>(query)
        .map { Pair(SqlSentence(it.value1, languageFrom, it.value2), SqlSentence(it.value3, languageTo, it.value4)) }
        .toMap()

    val sentenceWithUnknownWordsCount = result
        .map { Pair(it.key, countUnknownWords(it, attempted)) }
        .sortedBy { it.second }

    val minCount = sentenceWithUnknownWordsCount.first().second
    val sentencesWithMinDifficulty = sentenceWithUnknownWordsCount.takeWhile { it.second == minCount }

    logger.info("Min difficulty is $minCount and there are ${sentencesWithMinDifficulty.size} such sentences")

    val question = sentencesWithMinDifficulty.map { it.first }.shuffle(debugOptions.randomSeed).first()

    return Pair(question, result[question]!!)
  }

  private fun countUnknownWords(it: Map.Entry<SqlSentence, SqlSentence>, attempted: Set<SqlWord>): Int {
    return sentenceProvider.getOccurrences(it.key).count { !attempted.contains(it.word) }
  }

  private fun findNextByFrequencyWord(attempted: Collection<SqlWord>): SqlWord? {
    val occurrences = ContentDbConstants.wordOccurrencesTableName
    val words = ContentDbConstants.wordsTableName
    val ids = attempted.map { it.id }.joinToString(", ")
    val query = "SELECT id, language, lemma FROM $words WHERE id IN (SELECT wordId FROM $occurrences WHERE wordId NOT IN ($ids) GROUP BY wordId ORDER BY COUNT(*) DESC LIMIT 1)"
    return database!!.query3<Long, String, String>(query).map { wordFromTuple(it) }.singleOrNull()
  }

  private fun getScheduledWords(attemptCategory: String, attempted: Collection<SqlWord>): List<SqlWord> {
    val mapping = attempted.map { Pair(it.lemma, it) }.toMap()
    val scheduled = repetitionService.getScheduledEntities(attemptCategory, DateTime.now())
    return scheduled.map { mapping[it] }.filterNotNull()
  }

  private fun getAttemptedWords(attemptCategory: String): List<SqlWord> {
    val db = database!!
    val attemptedIds = repetitionService.getAttemptedItems(attemptCategory)

    val words = ContentDbConstants.wordsTableName
    val lemmas = attemptedIds.map { "'$it'" }.joinToString(", ")
    val query = "SELECT id, language, lemma FROM $words WHERE lemma IN ($lemmas)"

    return db.query3<Long, String, String>(query).map { wordFromTuple(it) }
  }

  private fun wordFromTuple(it: Tuple3<Long, String, String>) = SqlWord(it.value1, LanguageParser.parse(it.value2), it.value3)
}