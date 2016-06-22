package dataProcessor.db

import am5800.common.Language
import am5800.common.LearnGraphNode
import com.google.common.collect.Multimap
import org.tmatesoft.sqljet.core.table.ISqlJetTable
import org.tmatesoft.sqljet.core.table.SqlJetDb


class JetSqlLearnGraphWriter(private val sentenceWriter: JetSqlSentenceWriter, private val db: SqlJetDb) {
  fun write(map: Multimap<Language, LearnGraphNode>) {
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
    val name = "learnGraph"
    db.createTable("CREATE TABLE $name (wordId INTEGER, sentenceIds STRING, wordOrder INTEGER)")

    return db.getTable(name)
  }
}