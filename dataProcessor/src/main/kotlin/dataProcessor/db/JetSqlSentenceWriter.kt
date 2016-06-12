package dataProcessor.db

import am5800.common.*
import am5800.common.db.ContentDbConstants
import com.google.common.collect.LinkedHashMultimap
import org.tmatesoft.sqljet.core.table.ISqlJetTable
import org.tmatesoft.sqljet.core.table.SqlJetDb

class JetSqlSentenceWriter(private val db: SqlJetDb) : SentenceWriter {
  private class Tables(val sentencesTable: ISqlJetTable,
                       val sentenceTranslationsTable: ISqlJetTable,
                       val wordsTable: ISqlJetTable,
                       val wordOccurrencesTable: ISqlJetTable,
                       val languagesTable: ISqlJetTable)

  override fun write(occurrences: Set<WordOccurrence>) {
    val tables = getOrCreateTables()

    for (occurrence in occurrences) {
      getOccurrenceIdOrCreate(occurrence)

    }
  }

  private fun getWordIdOrWrite(word: Word, wordsTable: ISqlJetTable): Long {
    val existingId = wordsMapping[word]
    if (existingId != null) return existingId
    val wordId = wordsTable.insert(word.language.toString(), word.lemma)
    wordsMapping[word] = wordId
    return wordId
  }

  override fun write(translations: Map<Sentence, Sentence>) {
    val tables = getOrCreateTables()
    for ((s1, s2) in translations) {
      val s1Id = getSentenceIdOrWrite(s1, tables.sentencesTable)
      val s2Id = getSentenceIdOrWrite(s2, tables.sentencesTable)

      tables.sentenceTranslationsTable.insert(s1Id, s2Id)
    }

    writeLanguages(translations)
  }

  private val sentenceMapping = mutableMapOf<Sentence, Long>()
  private val wordsMapping = mutableMapOf<Word, Long>()
  private val occurrencesMapping = mutableMapOf<WordOccurrence, Long>()
  private var tables: Tables? = null

  private fun getOrCreateTables(): Tables {
    val instance = tables
    if (instance != null) return instance

    db.createTable("CREATE TABLE ${ContentDbConstants.sentenceTranslations} (key INTEGER PRIMARY KEY, value INTEGER)")
    db.createTable("CREATE TABLE ${ContentDbConstants.sentences} (id INTEGER PRIMARY KEY, language TEXT, text TEXT)")
    db.createTable("CREATE TABLE ${ContentDbConstants.sentenceLanguages} (knownLanguage TEXT, learnLanguage TEXT, count INTEGER)")
    db.createTable("CREATE TABLE ${ContentDbConstants.words} (id INTEGER PRIMARY KEY, language TEXT, lemma TEXT)")
    db.createTable("CREATE TABLE ${ContentDbConstants.wordOccurrences} (id INTEGER PRIMARY KEY, wordId INTEGER, sentenceId INTEGER, startIndex INTEGER, endIndex INTEGER)")
    db.createIndex("CREATE INDEX germanWordOccurrencesIndex ON ${ContentDbConstants.wordOccurrences} (wordId, sentenceId)")

    val sentencesTable = db.getTable(ContentDbConstants.sentences)
    val sentenceTranslationsTable = db.getTable(ContentDbConstants.sentenceTranslations)
    val wordsTable = db.getTable(ContentDbConstants.words)
    val wordOccurrencesTable = db.getTable(ContentDbConstants.wordOccurrences)
    val languagesTable = db.getTable(ContentDbConstants.sentenceLanguages)

    val result = Tables(sentencesTable, sentenceTranslationsTable, wordsTable, wordOccurrencesTable, languagesTable)
    tables = result
    return result
  }

  override fun write(sentences: List<Sentence>) {
    val tables = getOrCreateTables()

    for (sentence in sentences) {
      getSentenceIdOrWrite(sentence, tables.sentencesTable)
    }

  }

  private fun getSentenceIdOrWrite(sentence: Sentence, sentencesTable: ISqlJetTable): Long {
    val existingId = sentenceMapping[sentence]
    if (existingId != null) return existingId
    val insertedId = sentencesTable.insert(sentence.language.code, sentence.text)
    sentenceMapping[sentence] = insertedId
    return insertedId
  }

  private fun writeLanguages(sentenceTranslations: Map<Sentence, Sentence>) {
    val occurrenceBySentence = LinkedHashMultimap.create<Sentence, WordOccurrence>()
    for ((occurrence, id) in occurrencesMapping) {
      occurrenceBySentence.put(occurrence.sentence, occurrence)
    }

    val directions = EntityCounter<LanguagePair>()

    for ((left, right) in sentenceTranslations) {
      val leftOccurrences = occurrenceBySentence[left] ?: emptyList<WordOccurrence>()

      if (leftOccurrences.any()) directions.add(LanguagePair(right.language, left.language))
    }

    val table = getOrCreateTables().languagesTable

    for ((pair, count) in directions.result) {
      table.insert(pair.knownLanguage.code, pair.learnLanguage.code, count)
    }
  }

  fun getOccurrenceIdOrCreate(occurrence: WordOccurrence): Long {
    val tables = getOrCreateTables()
    val existingId = occurrencesMapping[occurrence]
    if (existingId != null) return existingId
    val wordId = getWordIdOrWrite(occurrence.word, tables.wordsTable)
    val sentenceId = getSentenceIdOrWrite(occurrence.sentence, tables.sentencesTable)
    val occurrenceId = tables.wordOccurrencesTable.insert(wordId, sentenceId, occurrence.startIndex, occurrence.endIndex)
    occurrencesMapping[occurrence] = occurrenceId
    return occurrenceId
  }
}