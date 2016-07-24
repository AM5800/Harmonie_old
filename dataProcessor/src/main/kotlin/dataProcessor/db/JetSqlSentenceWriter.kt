package dataProcessor.db

import am5800.common.Lemma
import am5800.common.LemmaOccurrence
import am5800.common.Sentence
import org.tmatesoft.sqljet.core.table.ISqlJetTable
import org.tmatesoft.sqljet.core.table.SqlJetDb

class JetSqlSentenceWriter(private val db: SqlJetDb) : SentenceWriter {
  override fun write(sentencePairs: List<Pair<Sentence, Sentence>>, occurrences: List<LemmaOccurrence>) {
    val sentenceToOccurrences = occurrences
        .groupBy { it.sentence }
        .map { Pair(it.key, it.value) }
        .toMap()

    for ((s1, s2) in sentencePairs) {
      write(s1, s2, sentenceToOccurrences)
    }
  }

  private fun write(s1: Sentence, s2: Sentence, sentenceToOccurrences: Map<Sentence, List<LemmaOccurrence>>) {
    val s1Id = write(s1, sentenceToOccurrences)
    val s2Id = write(s2, sentenceToOccurrences)
    val tables = ensureTables()
    tables.sentenceTranslationsTable.insert(s1Id, s2Id)
    tables.sentenceTranslationsTable.insert(s2Id, s1Id)
  }

  private fun write(sentence: Sentence, sentenceToOccurrences: Map<Sentence, List<LemmaOccurrence>>): Long {
    val sentenceId = write(sentence)

    val occurrences = sentenceToOccurrences[sentence]
    if (occurrences == null || occurrences.isEmpty()) return sentenceId

    write(sentenceId, occurrences)
    return sentenceId
  }

  private fun write(sentenceId: Long, occurrences: List<LemmaOccurrence>) {
    val occurrencesTable = ensureTables().lemmasOccurrencesTable

    for (occurrence in occurrences) {
      val lemmaId = getLemmaIdOrWrite(occurrence.lemma)

      occurrencesTable.insert(lemmaId, sentenceId, occurrence.startIndex, occurrence.endIndex)
    }
  }

  private fun getLemmaIdOrWrite(lemma: Lemma): Long {
    val existingId = lemmaIdToSqlId[lemma.id]
    if (existingId != null) return existingId

    val lemmasTable = ensureTables().lemmasTable
    val insertedId = lemmasTable.insert(lemma.lemma, lemma.language.code, lemma.partOfSpeech.toString(), lemma.difficultyLevel)
    lemmaIdToSqlId[lemma.id] = insertedId
    return insertedId
  }

  private fun write(sentence: Sentence): Long {
    val sentencesTable = ensureTables().sentencesTable
    val map = mutableMapOf<String, Any?>()
    map["uid"] = sentence.uid
    map["language"] = sentence.language.code
    map["text"] = sentence.text
    map["level"] = sentence.difficultyLevel

    val insertedId = sentencesTable.insertByFieldNames(map)
    return insertedId
  }

  private class Tables(val sentencesTable: ISqlJetTable,
                       val sentenceTranslationsTable: ISqlJetTable,
                       val lemmasTable: ISqlJetTable,
                       val lemmasOccurrencesTable: ISqlJetTable)

  private var tables: Tables? = null
  private val lemmaIdToSqlId = mutableMapOf<String, Long>()

  private fun ensureTables(): Tables {
    val instance = tables
    if (instance != null) return instance

    db.createTable("CREATE TABLE sentenceMapping (key INTEGER PRIMARY KEY, value INTEGER)")
    db.createTable("CREATE TABLE sentences (id INTEGER PRIMARY KEY, uid STRING, language STRING, text STRING, level INTEGER)")
    db.createTable("CREATE TABLE lemmas (id INTEGER PRIMARY KEY, value STRING, language STRING, pos STRING, level INTEGER)")
    db.createTable("CREATE TABLE lemmaOccurrences (id INTEGER PRIMARY KEY, lemmaId INTEGER, sentenceId INTEGER, startIndex INTEGER, endIndex INTEGER)")
    db.createIndex("CREATE INDEX lemmaOccurrencesOccurrencesIndex ON lemmaOccurrences (lemmaId, sentenceId)")
    db.createIndex("CREATE INDEX sentencesUidIndex ON sentences (uid)")

    val sentencesTable = db.getTable("sentences")
    val sentenceTranslationsTable = db.getTable("sentenceMapping")
    val lemmasTable = db.getTable("lemmas")
    val lemmasOccurrencesTable = db.getTable("lemmaOccurrences")

    val result = Tables(sentencesTable, sentenceTranslationsTable, lemmasTable, lemmasOccurrencesTable)
    tables = result
    return result
  }
}