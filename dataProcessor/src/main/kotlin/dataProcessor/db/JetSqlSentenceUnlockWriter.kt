package dataProcessor.db

import am5800.common.Language
import com.google.common.collect.Multimap
import dataProcessor.UnlockInfo
import org.tmatesoft.sqljet.core.table.ISqlJetTable
import org.tmatesoft.sqljet.core.table.SqlJetDb


class JetSqlSentenceUnlockWriter(private val sentenceWriter: JetSqlSentenceWriter, private val db: SqlJetDb) {
  fun write(map: Multimap<Language, UnlockInfo>) {
    val table = ensureTable()
    for ((language, infos) in map.asMap()) {
      infos.forEachIndexed { i, unlockInfo ->
        val wordId = sentenceWriter.getWordIdOrCreate(unlockInfo.word)
        val sentenceIds = unlockInfo.sentences.map { sentenceWriter.getSentenceIdOrCreate(it) }.joinToString(";")

        table.insert(wordId, sentenceIds, i)
      }
    }
  }

  private fun ensureTable(): ISqlJetTable {
    val name = "sentenceUnlockOrder"
    db.createTable("CREATE TABLE $name (wordId INTEGER, sentenceId INTEGER, wordOrder INTEGER)")

    return db.getTable(name)
  }
}