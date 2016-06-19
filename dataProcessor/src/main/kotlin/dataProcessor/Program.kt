package dataProcessor

import am5800.common.Word
import dataProcessor.db.JetSqlFillTheGapsWriter
import dataProcessor.db.JetSqlSentenceUnlockWriter
import dataProcessor.db.JetSqlSentenceWriter
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.table.SqlJetDb
import java.io.File

fun main(args: Array<String>) {
  val corpusDir = File("data/corpus")
  val corpuses = corpusDir
      .listFiles { file -> file.extension.equals("xml", true) }.toList()

  val counts = loadCounts(corpusDir)

  run(corpuses, File("androidApp/src/main/assets/content.db"), counts)
  run(listOf(File(corpusDir, "test")), File("data/test.db"), counts)
}

fun loadCounts(corpusDir: File): Map<Word, Int> {
  return CountsParser().parse(File(corpusDir, "counts"))
}

private fun run(corpuses: Collection<File>, outFile: File, counts: Map<Word, Int>) {
  val parseResult = loadData(corpuses).merge()
  val db = openDb(outFile)
  db.runTransaction({ transaction ->
    val sentenceWriter = JetSqlSentenceWriter(db)

    sentenceWriter.write(parseResult.sentences)
    sentenceWriter.write(parseResult.occurrences)
    sentenceWriter.write(parseResult.translations)

    val fillTheGapsWriter = JetSqlFillTheGapsWriter(db, sentenceWriter)
    createFillTheGaps(parseResult, fillTheGapsWriter)
    writeSentenceUnlocks(counts, db, parseResult, sentenceWriter)

  }, SqlJetTransactionMode.WRITE)


  db.close()
}

private fun writeSentenceUnlocks(counts: Map<Word, Int>, db: SqlJetDb, parseResult: ParseResult, sentenceWriter: JetSqlSentenceWriter) {
  val filteredCounts = counts.filter { it.value > 5 }
  val unlockInfos = SentenceUnlocker.createUnlockOrder(filteredCounts, parseResult.occurrences)
  val unlockWriter = JetSqlSentenceUnlockWriter(sentenceWriter, db)
  unlockWriter.write(unlockInfos)
}

fun openDb(path: File): SqlJetDb {
  if (path.exists() && !path.delete()) throw Exception("Can't delete previous database")
  val database = SqlJetDb.open(path, true)
  return database
}


fun loadData(corpuses: Collection<File>): Collection<ParseResult> {
  val parser = HarmonieSentencesParser()

  return corpuses.map { parser.parse(it) }
}


