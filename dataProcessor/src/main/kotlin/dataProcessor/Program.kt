package dataProcessor

import am5800.common.Sentence
import com.google.common.collect.LinkedHashMultimap
import dataProcessor.db.JetSqlSentenceWriter
import dataProcessor.parsing.LemmasXmlParser
import dataProcessor.parsing.SentencesXmlParser
import dataProcessor.parsing.merge
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.table.SqlJetDb
import java.io.File

fun main(args: Array<String>) {
  val corpusDir = File("data/corpus")

  val lemmasFiles = corpusDir.listFiles { file -> file.name.startsWith("ls_", true) }.toList()
  val sentenceFiles = corpusDir.listFiles { file -> file.name.startsWith("ss_", true) }.toList()

  run(lemmasFiles, sentenceFiles, File("androidApp/src/main/assets/content.db"))
  //run(listOf(File(corpusDir, "test")), File("data/test.db"))
}

private fun run(lemmasFiles: Collection<File>, sentenceFiles: Collection<File>, outFile: File) {
  val lemmasParser = LemmasXmlParser()
  val allLemmas = lemmasFiles.flatMap { lemmasParser.parse(it) }.distinct()

  val sentenceParser = SentencesXmlParser()
  val sentenceParseResults = sentenceFiles.map { sentenceParser.parse(it, allLemmas) }.merge()

  val db = openDb(outFile)
  db.runTransaction({ transaction ->
    val sentenceWriter = JetSqlSentenceWriter(transaction)
    val pairs = getSentencePairs(sentenceParseResults.sentences)
    sentenceWriter.write(pairs, sentenceParseResults.occurrences)
  }, SqlJetTransactionMode.WRITE)


  db.close()
}

fun getSentencePairs(allSentences: List<Sentence>): List<Pair<Sentence, Sentence>> {
  val byId = LinkedHashMultimap.create <String, Sentence>()
  for (sentence in allSentences) {
    byId.put(sentence.uid, sentence)
  }

  return byId.asMap()
      .filter { it.value.size == 2 }
      .map { Pair(it.value.first(), it.value.last()) }
}

fun openDb(path: File): SqlJetDb {
  if (path.exists() && !path.delete()) throw Exception("Can't delete previous database")
  val database = SqlJetDb.open(path, true)
  return database
}


