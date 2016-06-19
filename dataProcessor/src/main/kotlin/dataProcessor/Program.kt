package dataProcessor

import am5800.common.Word
import dataProcessor.db.JetSqlFillTheGapsWriter
import dataProcessor.db.JetSqlSentenceWriter
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.table.SqlJetDb
import java.io.File

fun main(args: Array<String>) {
  val corpusDir = File("data/corpus")
  val corpuses = corpusDir
      .listFiles { file -> file.extension.equals("xml", true) }.toList()

  val counts = loadCounts(corpusDir)

  run(corpuses, File("androidApp/src/main/assets/content.db"), corpusDir)
  run(listOf(File(corpusDir, "test")), File("data/test.db"), corpusDir)
}

fun loadCounts(corpusDir: File): Map<Word, Int> {
  return CountsParser().parse(File(corpusDir, "counts"))
}

private fun run(corpuses: Collection<File>, outFile: File, corpusDir: File) {
  val parseResult = loadData(corpuses).merge()
  val db = openDb(outFile)
  db.runTransaction({ transaction ->
    val sentenceWriter = JetSqlSentenceWriter(db)

    sentenceWriter.write(parseResult.sentences)
    sentenceWriter.write(parseResult.occurrences)
    sentenceWriter.write(parseResult.translations)

    val fillTheGapsWriter = JetSqlFillTheGapsWriter(db, sentenceWriter)
    createFillTheGaps(parseResult, fillTheGapsWriter)
    WordsOrderCreator.createWordsOrder(CountsParser().parse(File(corpusDir, "counts")), parseResult.occurrences)
  }, SqlJetTransactionMode.WRITE)


  db.close()
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


