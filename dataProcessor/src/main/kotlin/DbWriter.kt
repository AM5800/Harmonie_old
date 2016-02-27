import com.google.common.collect.Multimap
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.table.SqlJetDb
import java.io.File

class DbWriter {
  private val sentencesTableName = "sentences"
  private val sentenceMappingTableName = "sentenceMapping"
  private val wordsTableName = "words"
  private val wordOccurrencesTableName = "wordOccurrences"

  fun write(path: File, sentencesData: Pair<Map<Long, Long>, List<Sentence>>, occurrences: Multimap<Word, Long>, frequencies: Map<Word, Double>) {
    path.delete()
    val database = SqlJetDb.open(path, true)
    database.runTransaction({ db ->
      createDbSchema(db)
      val sentenceMapping = writeSentences(sentencesData, db)
      writeWords(occurrences, frequencies, sentenceMapping, db)
    }, SqlJetTransactionMode.WRITE)
  }

  private fun writeWords(wordsOccurrences: Multimap<Word, Long>, frequencies: Map<Word, Double>, sentenceMapping: Map<Long, Long>, db: SqlJetDb) {
    val wordsTable = db.getTable(wordsTableName)
    val occurrencesTable = db.getTable(wordOccurrencesTableName)

    for (pair in wordsOccurrences.asMap()) {
      val word = pair.key
      val frequency = frequencies[word] ?: 0
      val lang = LanguageParser.toShort(word.language)

      val wordId = wordsTable.insert(lang, word.word, frequency)
      val occurrences = pair.value.map { sentenceMapping[it]!! }
      for (occurrence in occurrences) {
        occurrencesTable.insert(wordId, occurrence)
      }
    }
  }

  private fun writeSentences(sentencesData: Pair<Map<Long, Long>, List<Sentence>>, db: SqlJetDb): Map<Long, Long> {
    val result = mutableMapOf<Long, Long>()
    val sentences = sentencesData.second
    val translations = sentencesData.first

    val sentencesTable = db.getTable(sentencesTableName)
    sentences.forEachIndexed { i, sentence ->
      val id = sentencesTable.insert(LanguageParser.toShort(sentence.language), sentence.text)
      result.put(i.toLong(), id)
    }

    val sentencesMappingTable = db.getTable(sentenceMappingTableName)
    for (pair in translations) {
      val from = result[pair.key]!!
      val to = result[pair.value]!!
      sentencesMappingTable.insert(from, to)
    }

    return result
  }

  private fun createDbSchema(db: SqlJetDb) {
    db.createTable("CREATE TABLE $sentencesTableName (id INTEGER PRIMARY KEY, lang TEXT, text TEXT)")
    db.createTable("CREATE TABLE $sentenceMappingTableName (key INTEGER PRIMARY KEY, value INTEGER)")
    db.createTable("CREATE TABLE $wordsTableName (key INTEGER PRIMARY KEY, lang TEXT, word TEXT, frequency REAL)")
    db.createTable("CREATE TABLE $wordOccurrencesTableName (wordId INTEGER, sentenceId INTEGER)")
    //db.createIndex("CREATE INDEX germanWordOccurrencesIndex ON wordOccurrences (wordId)")
  }
}