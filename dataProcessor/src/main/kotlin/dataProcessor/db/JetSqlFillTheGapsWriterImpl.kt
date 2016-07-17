package dataProcessor.db

import am5800.common.db.ContentDbConstants
import dataProcessor.FormOccurrence
import org.tmatesoft.sqljet.core.table.ISqlJetTable
import org.tmatesoft.sqljet.core.table.SqlJetDb

class JetSqlFillTheGapsWriter(private val db: SqlJetDb, private val sentenceWriter: JetSqlSentenceWriter) : FillTheGapsWriter {
  override fun write(result: List<FormOccurrence>) {
    val table = getOrCreateTable()

    for ((form, topic, occurrence) in result) {
      val wordOccurrenceId = sentenceWriter.getOccurrenceId(occurrence)
      table.insert(form, topic, wordOccurrenceId)
    }
  }

  private fun getOrCreateTable(): ISqlJetTable {
    db.createTable("CREATE TABLE IF NOT EXISTS ${ContentDbConstants.fillTheGapOccurrences} (form TEXT, topic TEXT, occurrenceId INTEGER)")
    db.createIndex("CREATE INDEX IF NOT EXISTS fillTheGapFormIndex ON ${ContentDbConstants.fillTheGapOccurrences} (form)")

    return db.getTable(ContentDbConstants.fillTheGapOccurrences)
  }
}