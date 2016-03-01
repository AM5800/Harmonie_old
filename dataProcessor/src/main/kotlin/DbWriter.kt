import am5800.common.ContentDbConstants
import am5800.common.LanguageParser
import am5800.common.Sentence
import com.google.common.collect.Multimap
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.table.SqlJetDb
import java.io.File

class DbWriter {


  fun write(path: File, data: Data) {
    path.delete()
    val database = SqlJetDb.open(path, true)
    database.runTransaction({ db ->
      createDbSchema(db)
      val sentenceMapping = writeSentences(data.sentences, data.sentenceTranslations, db)
      writeWords(data.wordOccurrences, data.wordFrequencies, sentenceMapping, db)
    }, SqlJetTransactionMode.WRITE)
  }

  private fun writeWords(wordsOccurrences: Multimap<Word, Long>, frequencies: Map<Word, Double>, sentenceMapping: Map<Long, Long>, db: SqlJetDb) {
    val wordsTable = db.getTable(ContentDbConstants.wordsTableName)
    val occurrencesTable = db.getTable(ContentDbConstants.wordOccurrencesTableName)

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

  private fun writeSentences(sentences: List<Sentence>, translations: Map<Long, Long>, db: SqlJetDb): Map<Long, Long> {
    val result = mutableMapOf<Long, Long>()

    val sentencesTable = db.getTable(ContentDbConstants.sentencesTableName)
    sentences.forEachIndexed { i, sentence ->
      val id = sentencesTable.insert(LanguageParser.toShort(sentence.language), sentence.text)
      result.put(i.toLong(), id)
    }

    val sentencesMappingTable = db.getTable(ContentDbConstants.sentenceMappingTableName)
    for (pair in translations) {
      val from = result[pair.key]!!
      val to = result[pair.value]!!
      sentencesMappingTable.insert(from, to)
    }

    return result
  }

  private fun createDbSchema(db: SqlJetDb) {
    db.createTable("CREATE TABLE ${ContentDbConstants.sentencesTableName} (id INTEGER PRIMARY KEY, lang TEXT, text TEXT)")
    db.createTable("CREATE TABLE ${ContentDbConstants.sentenceMappingTableName} (key INTEGER PRIMARY KEY, value INTEGER)")
    db.createTable("CREATE TABLE ${ContentDbConstants.wordsTableName} (key INTEGER PRIMARY KEY, lang TEXT, word TEXT, frequency REAL)")
    db.createTable("CREATE TABLE ${ContentDbConstants.wordOccurrencesTableName} (wordId INTEGER, sentenceId INTEGER)")
    //db.createIndex("CREATE INDEX germanWordOccurrencesIndex ON wordOccurrences (wordId)")
  }
}