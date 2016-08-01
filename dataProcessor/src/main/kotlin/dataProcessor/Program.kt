package dataProcessor

import am5800.common.Sentence
import com.google.common.collect.LinkedHashMultimap
import dataProcessor.db.JetSqlMeaningsWriter
import dataProcessor.db.JetSqlSentenceWriter
import dataProcessor.parsing.*
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.table.SqlJetDb
import java.io.File

fun main(args: Array<String>) {
  val corpusDir = File("data/corpus")

  val lemmasFiles = corpusDir.listFiles { file -> file.name.startsWith("ls_", true) }.toList()
  val sentenceFiles = corpusDir.listFiles { file -> file.name.startsWith("ss_", true) }.toList()
  val meaningsFiles = corpusDir.listFiles { file -> file.name.startsWith("ms_", true) }.toList()

  run(lemmasFiles, sentenceFiles, meaningsFiles, File("androidApp/src/main/assets/content.db"))

  run(listOf(File(corpusDir, "test_ls_de.xml")),
      listOf(File(corpusDir, "test_ss_de.xml"), File(corpusDir, "test_ss_ru.xml")),
      emptyList(),
      File("data/test.db"))
}

private fun run(lemmasFiles: Collection<File>, sentenceFiles: Collection<File>, meaningsFiles: Collection<File>, outFile: File) {
  val lemmasParser = LemmasXmlParser()
  val allLemmas = lemmasFiles.flatMap { lemmasParser.parse(it) }.distinct()

  val sentenceParser = SentencesXmlParser()
  val sentenceParseResults = sentenceFiles.map { sentenceParser.parse(it, allLemmas) }.merge()

  val meaningsParser = MeaningsXmlParser()
  val idToLemma = allLemmas.map { Pair(it.id, it) }.toMap()
  val meanings = meaningsFiles.map { meaningsParser.parse(it, idToLemma) }.mergeByLanguages()


  val db = openDb(outFile)
  db.runTransaction({ transaction ->
    val sentenceWriter = JetSqlSentenceWriter(transaction)
    val meaningsWriter = JetSqlMeaningsWriter(sentenceWriter, transaction)
    val pairs = getSentencePairs(sentenceParseResults.sentences)
    sentenceWriter.write(pairs, sentenceParseResults.occurrences)
    meaningsWriter.write(meanings)
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


