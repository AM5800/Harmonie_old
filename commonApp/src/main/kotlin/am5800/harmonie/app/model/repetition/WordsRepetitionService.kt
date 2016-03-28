package am5800.harmonie.app.model.repetition

import am5800.common.Language
import am5800.common.code
import am5800.common.db.ContentDbConstants
import am5800.common.Word
import am5800.harmonie.app.model.dbAccess.sql.ContentDb
import am5800.harmonie.app.model.dbAccess.sql.ContentDbConsumer
import am5800.harmonie.app.model.dbAccess.sql.SqlWord
import am5800.harmonie.app.model.dbAccess.sql.query2
import org.joda.time.DateTime

interface WordsRepetitionService {
  fun submitAttempt(word: Word, score: AttemptScore): DateTime
  fun computeDueDate(word: Word, score: AttemptScore): DateTime
  fun getScheduledWords(language: Language, dateTime: DateTime): List<Word>
  fun getAttemptedWords(language: Language): List<Word>

  fun getBinaryWordScore(word: Word): BinaryLearnScore?
  fun getAverageBinaryScore(language: Language): Double
}

class WordsRepetitionServiceImpl(private val repetitionService: RepetitionService) : WordsRepetitionService, ContentDbConsumer {
  override fun getBinaryWordScore(word: Word): BinaryLearnScore? {
    return repetitionService.getBinaryScore(word.lemma, getCategory(word.language))
  }

  override fun getAverageBinaryScore(language: Language): Double {
    return getAttemptedWords(language)
        .map { getBinaryWordScore(it) }.filterNotNull()
        .map {
          when (it) {
            BinaryLearnScore.Good -> 1.0
            BinaryLearnScore.Bad -> 0.0
          }
        }.average()
  }

  private val cache = mutableMapOf<Pair<String, Language>, SqlWord>()

  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {
  }

  private var database: ContentDb? = null

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  private val attemptCategory = "ParallelSentenceWords"
  private fun getCategory(language: Language) = attemptCategory + language.code()

  override fun submitAttempt(word: Word, score: AttemptScore): DateTime {
    val category = getCategory(word.language)
    return repetitionService.submitAttempt(word.lemma, category, score)
  }

  override fun computeDueDate(word: Word, score: AttemptScore): DateTime {
    val category = getCategory(word.language)
    return repetitionService.computeDueDate(word.lemma, category, score)
  }

  override fun getScheduledWords(language: Language, dateTime: DateTime): List<Word> {
    val category = getCategory(language)
    val scheduled = repetitionService.getScheduledEntities(category, DateTime.now())
    return getWords(scheduled, language)
  }

  private fun getWords(lemmas: List<String>, language: Language): List<Word> {
    val lemmasToSearch = mutableListOf<String>()
    val result = mutableListOf<SqlWord>()

    for (lemma in lemmas) {
      val key = Pair(lemma, language)
      val cached = cache[key]
      if (cached == null) lemmasToSearch.add(lemma)
      else result.add(cached)
    }

    if (lemmasToSearch.isEmpty()) return result

    val words = ContentDbConstants.wordsTableName
    val joinedLemmas = lemmasToSearch.map { "'$it'" }.joinToString(", ")
    val langCode = language.code()
    val query = "SELECT id, lemma FROM $words WHERE language='$langCode' AND lemma IN ($joinedLemmas)"
    val queryResult = database!!.query2<Long, String>(query).map { SqlWord(it.first, language, it.second) }
    for (word in queryResult) {
      val key = Pair(word.lemma, language)
      cache[key] = word
      result.add(word)
    }

    return result
  }

  override fun getAttemptedWords(language: Language): List<Word> {
    val attemptedIds = repetitionService.getAttemptedItems(getCategory(language))
    return getWords(attemptedIds, language)
  }
}