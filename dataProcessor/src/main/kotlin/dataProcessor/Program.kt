package dataProcessor

import dataProcessor.db.JetSqlFillTheGapsWriter
import dataProcessor.db.JetSqlSentenceWriter
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.table.SqlJetDb
import java.io.File

fun main(args: Array<String>) {
  val corpusDir = File("data/corpus")
  val corpuses = corpusDir
      .listFiles { file -> file.extension.equals("xml", true) }.toList()

  run(corpuses, File("androidApp/src/main/assets/content.db"))
  run(listOf(File(corpusDir, "test")), File("data/test.db"))
}

private fun run(corpuses: Collection<File>, outFile: File) {
  val parseResult = loadData(corpuses).merge()
  val db = openDb(outFile)
  db.runTransaction({ transaction ->
    val sentenceWriter = JetSqlSentenceWriter(transaction)

    sentenceWriter.write(parseResult.sentences, parseResult.sentenceLevels)
    sentenceWriter.write(parseResult.occurrences, parseResult.wordLevels)
    sentenceWriter.write(parseResult.translations, parseResult.sentenceLevels)

    val fillTheGapsWriter = JetSqlFillTheGapsWriter(transaction, sentenceWriter)
    createFillTheGaps(parseResult, fillTheGapsWriter)

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


