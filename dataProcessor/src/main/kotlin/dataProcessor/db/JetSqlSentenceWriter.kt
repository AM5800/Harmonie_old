package dataProcessor.db

import am5800.common.*
import com.google.common.collect.LinkedHashMultimap
import org.tmatesoft.sqljet.core.table.ISqlJetTable
import org.tmatesoft.sqljet.core.table.SqlJetDb

class JetSqlSentenceWriter(private val db: SqlJetDb) : SentenceWriter {
  private class Tables(val sentencesTable: ISqlJetTable,
                       val sentenceTranslationsTable: ISqlJetTable,
                       val wordsTable: ISqlJetTable,
                       val wordOccurrencesTable: ISqlJetTable,
                       val languagesTable: ISqlJetTable)

  override fun write(occurrences: Set<WordOccurrence>, levels: Map<Word, Int?>) {
    for (occurrence in occurrences) {
      getOccurrenceIdOrCreate(occurrence, levels)
    }
  }

  fun getWordIdOrWrite(word: Word, levels: Map<Word, Int?>): Long {
    val wordsTable = ensureTables().wordsTable

    val existingId = wordsMapping[word]
    if (existingId != null) return existingId
    val wordId = wordsTable.insert(word.language.code, word.lemma, levels[word])
    wordsMapping[word] = wordId
    return wordId
  }

  override fun write(translations: Map<Sentence, Sentence>, levels: Map<Sentence, Int?>) {
    val tables = ensureTables()
    for ((s1, s2) in translations) {
      val s1Id = getSentenceIdOrWrite(s1, levels)
      val s2Id = getSentenceIdOrWrite(s2, levels)

      tables.sentenceTranslationsTable.insert(s1Id, s2Id)
    }

    writeLanguages(translations)
  }

  private val sentenceMapping = mutableMapOf<Sentence, Long>()
  private val wordsMapping = mutableMapOf<Word, Long>()
  private val occurrencesMapping = mutableMapOf<WordOccurrence, Long>()
  private var tables: Tables? = null

  private fun ensureTables(): Tables {
    val instance = tables
    if (instance != null) return instance

    db.createTable("CREATE TABLE sentenceMapping (key INTEGER PRIMARY KEY, value INTEGER)")
    db.createTable("CREATE TABLE sentences (id INTEGER PRIMARY KEY, uid STRING, language TEXT, text TEXT, level INTEGER)")
    db.createTable("CREATE TABLE sentenceLanguages (knownLanguage TEXT, learnLanguage TEXT, count INTEGER)")
    db.createTable("CREATE TABLE words (id INTEGER PRIMARY KEY, language TEXT, lemma TEXT, level INTEGER)")
    db.createTable("CREATE TABLE wordOccurrences (id INTEGER PRIMARY KEY, wordId INTEGER, sentenceId INTEGER, startIndex INTEGER, endIndex INTEGER)")
    db.createIndex("CREATE INDEX germanWordOccurrencesIndex ON wordOccurrences (wordId, sentenceId)")
    db.createIndex("CREATE INDEX sentencesUidIndex ON sentences (uid)")

    val sentencesTable = db.getTable("sentences")
    val sentenceTranslationsTable = db.getTable("sentenceMapping")
    val wordsTable = db.getTable("words")
    val wordOccurrencesTable = db.getTable("wordOccurrences")
    val languagesTable = db.getTable("sentenceLanguages")

    val result = Tables(sentencesTable, sentenceTranslationsTable, wordsTable, wordOccurrencesTable, languagesTable)
    tables = result
    return result
  }

  override fun write(sentences: List<Sentence>, levels: Map<Sentence, Int?>) {
    for (sentence in sentences) {
      getSentenceIdOrWrite(sentence, levels)
    }
  }

  fun getSentenceIdOrWrite(sentence: Sentence, levels: Map<Sentence, Int?>): Long {
    val existingId = sentenceMapping[sentence]
    if (existingId != null) return existingId
    val sentencesTable = ensureTables().sentencesTable
    val map = mutableMapOf<String, Any?>()

    if (sentence.uid != null) map["uid"] = sentence.uid
    map["language"] = sentence.language.code
    map["text"] = sentence.text
    map["level"] = levels[sentence]

    val insertedId = sentencesTable.insertByFieldNames(map)
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

    val table = ensureTables().languagesTable

    for ((pair, count) in directions.result) {
      table.insert(pair.knownLanguage.code, pair.learnLanguage.code, count)
    }
  }

  fun getOccurrenceIdOrCreate(occurrence: WordOccurrence, levels: Map<Word, Int?>): Long {
    val tables = ensureTables()
    val existingId = occurrencesMapping[occurrence]
    if (existingId != null) return existingId
    val wordId = getWordIdOrWrite(occurrence.word, levels)
    val sentenceId = getSentenceId(occurrence.sentence)
    val occurrenceId = tables.wordOccurrencesTable.insert(wordId, sentenceId, occurrence.startIndex, occurrence.endIndex)
    occurrencesMapping[occurrence] = occurrenceId
    return occurrenceId
  }

  private fun getSentenceId(sentence: Sentence): Long {
    return sentenceMapping[sentence] ?: throw Exception("Sentence not found")
  }

  fun getOccurrenceId(occurrence: WordOccurrence): Long {
    return occurrencesMapping[occurrence] ?: throw Exception("Occurrence not found: " + occurrence.word)
  }
}