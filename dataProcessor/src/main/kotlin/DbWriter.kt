import am5800.common.LanguageParser
import am5800.common.db.ContentDbConstants
import am5800.common.db.DbWordOccurrence
import am5800.common.db.Sentence
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.table.ISqlJetTable
import org.tmatesoft.sqljet.core.table.SqlJetDb
import java.io.File

class DbWriter {
  fun write(path: File, data: Data) {
    path.delete()
    val database = SqlJetDb.open(path, true)
    database.runTransaction({ db ->
      createDbSchema(db)
      val sentenceMapping = writeSentences(data.sentenceTranslations, db)
      writeWords(data.wordOccurrences, sentenceMapping, db)
    }, SqlJetTransactionMode.WRITE)
  }

  private fun writeWords(wordsOccurrences: List<DbWordOccurrence>, sentenceMapping: Map<Sentence, Long>, db: SqlJetDb) {
    val wordsTable = db.getTable(ContentDbConstants.wordsTableName)
    val occurrencesTable = db.getTable(ContentDbConstants.wordOccurrencesTableName)

    for (occurrencePair in wordsOccurrences.groupBy { it.word }) {
      val word = occurrencePair.key
      val lang = LanguageParser.toShortString(word.language)

      val wordId = wordsTable.insert(lang, word.lemma)
      for (occurrence in occurrencePair.value) {
        val sentenceId = sentenceMapping[occurrence.sentence]
        occurrencesTable.insert(wordId, sentenceId, occurrence.startIndex, occurrence.endIndex)
      }
    }
  }

  private fun writeSentences(translations: Map<Sentence, Sentence>, db: SqlJetDb): Map<Sentence, Long> {
    val sentenceIdToRealId = mutableMapOf<Sentence, Long>()

    val sentencesTable = db.getTable(ContentDbConstants.sentencesTableName)

    val sentenceTranslations = db.getTable(ContentDbConstants.sentenceTranslationsTableName)
    for (pair in translations) {
      val from = getOrInsert(pair.key, sentenceIdToRealId, sentencesTable)
      val to = getOrInsert(pair.value, sentenceIdToRealId, sentencesTable)
      sentenceTranslations.insert(from, to)
    }

    return sentenceIdToRealId
  }

  private fun getOrInsert(sentence: Sentence, sentenceToId: MutableMap<Sentence, Long>, sentencesTable: ISqlJetTable): Long {
    val id = sentenceToId[sentence]
    if (id == null) {
      val insertedId = sentencesTable.insert(LanguageParser.toShortString(sentence.language), sentence.text)
      sentenceToId[sentence] = insertedId
      return insertedId
    } else return id
  }

  private fun createDbSchema(db: SqlJetDb) {
    db.createTable("CREATE TABLE ${ContentDbConstants.sentencesTableName} (id INTEGER PRIMARY KEY, language TEXT, text TEXT)")
    db.createTable("CREATE TABLE ${ContentDbConstants.sentenceTranslationsTableName} (key INTEGER PRIMARY KEY, value INTEGER)")
    db.createTable("CREATE TABLE ${ContentDbConstants.wordsTableName} (id INTEGER PRIMARY KEY, language TEXT, lemma TEXT)")
    db.createTable("CREATE TABLE ${ContentDbConstants.wordOccurrencesTableName} (wordId INTEGER, sentenceId INTEGER, startIndex INTEGER, endIndex INTEGER)")
    db.createIndex("CREATE INDEX germanWordOccurrencesIndex ON ${ContentDbConstants.wordOccurrencesTableName} (wordId, sentenceId)")
  }
}