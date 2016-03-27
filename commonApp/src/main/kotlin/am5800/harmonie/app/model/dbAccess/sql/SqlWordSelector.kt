package am5800.harmonie.app.model.dbAccess.sql

import am5800.common.Language
import am5800.common.code
import am5800.common.db.ContentDbConstants
import am5800.common.db.Word
import am5800.harmonie.app.model.WordSelector
import am5800.harmonie.app.model.WordSelectorAlgorithm
import am5800.harmonie.app.model.dbAccess.KeyValueDatabase
import am5800.harmonie.app.model.repetition.WordsRepetitionService

class SqlWordSelector(private val wordsRepetitionService: WordsRepetitionService, private val keyValueDatabase: KeyValueDatabase) : WordSelector, ContentDbConsumer {
  override fun dbMigrationPhase1(oldDb: ContentDb) {

  }

  override fun dbMigrationPhase2(newDb: ContentDb) {

  }

  private var database: ContentDb? = null

  override fun dbInitialized(db: ContentDb) {
    database = db
  }

  override fun findBestWord(language: Language): Word? {
    val orderedWords = getOrderedWords(language)

    val settingsKey = "latestSelected-${language.code()}"
    val prevLemma = keyValueDatabase.tryGetValue(settingsKey)
    val prevWord = if (prevLemma == null) null else Word(language, prevLemma)

    val result = WordSelectorAlgorithm.selectNextWord(orderedWords, prevWord, wordsRepetitionService.getAverageBinaryScore(language), { wordsRepetitionService.getBinaryWordScore(it) })

    if (result == null) keyValueDatabase.remove(settingsKey)
    else keyValueDatabase.setValue(settingsKey, result.lemma)

    return result
  }

  private fun getOrderedWords(language: Language): List<SqlWord> {
    val counts = ContentDbConstants.wordCountsTableName
    val words = ContentDbConstants.wordsTableName
    val lang = language.code()
    val query = """
        SELECT $words.id, $words.lemma
          FROM $words
          INNER JOIN $counts
            ON $counts.wordId = $words.id
          WHERE $words.language='$lang'
          ORDER BY $counts.count DESC
      """
    return database!!.query2<Long, String>(query).map { SqlWord(it.first, language, it.second) }
  }
}