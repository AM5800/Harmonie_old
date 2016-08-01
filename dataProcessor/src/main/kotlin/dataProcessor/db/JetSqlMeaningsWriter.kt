package dataProcessor.db

import dataProcessor.parsing.MeaningsParseResult
import org.tmatesoft.sqljet.core.table.ISqlJetTable
import org.tmatesoft.sqljet.core.table.SqlJetDb

class JetSqlMeaningsWriter(private val sentenceWriter: JetSqlSentenceWriter, private val transaction: SqlJetDb) : MeaningsWriter {
  override fun write(data: List<MeaningsParseResult>) {
    val table = ensureTable()
    for (mpr in data) {
      val meaningsLanguage = mpr.meaningsLanguage
      for ((lemma, meanings) in mpr.meanings.asMap()) {
        val lemmaSqlId = sentenceWriter.getLemmaSqlId(lemma) ?: continue

        val meaningsString = meanings.joinToString("; ")
        table.insert(lemmaSqlId, meaningsLanguage.code, meaningsString)
      }
    }
  }

  private fun ensureTable(): ISqlJetTable {
    transaction.createTable("CREATE TABLE IF NOT EXISTS meanings (lemmaId INTEGER PRIMARY KEY, meaningsLanguage TEXT, meanings TEXT)")
    return transaction.getTable("meanings")
  }
}