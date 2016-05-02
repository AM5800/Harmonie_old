package dataProcessor

import am5800.common.*
import am5800.common.db.ContentDbConstants
import com.google.common.collect.LinkedHashMultimap
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.table.ISqlJetTable
import org.tmatesoft.sqljet.core.table.SqlJetDb
import java.io.File

class DbWriter {
  fun write(path: File, data: Data) {
    if (path.exists() && !path.delete()) throw Exception("Can't delete previous database")
    val database = SqlJetDb.open(path, true)
    database.runTransaction({ db ->
      createDbSchema(db)
      val sentenceMapping = writeSentences(data.sentenceTranslations, db)
      val occurrenceMapping = writeWords(data.wordOccurrences, sentenceMapping, data.realWorldWordsCount, db)
      writeDifficulties(data.difficulties, sentenceMapping, db)
      writeLanguageSupport(data.sentenceTranslations, data.wordOccurrences, db)
      writeFillTheGaps(data.fillTheGapOccurrences, occurrenceMapping, db)
    }, SqlJetTransactionMode.WRITE)
  }

  private fun writeFillTheGaps(fillTheGaps: List<FormOccurrence>,
                               occurrenceMapping: Map<WordOccurrence, Long>, db: SqlJetDb) {
    val table = db.getTable(ContentDbConstants.fillTheGapOccurrences)

    for (formOccurrence in fillTheGaps) {
      val occurrence = formOccurrence.occurrence
      val topic = formOccurrence.topic
      val wordOccurrenceId = occurrenceMapping[occurrence]!!
      table.insert(formOccurrence.form, topic, wordOccurrenceId)
    }
  }

  private fun writeLanguageSupport(sentenceTranslations: Map<Sentence, Sentence>, wordOccurrences: List<WordOccurrence>, db: SqlJetDb) {
    val occurrenceBySentence = LinkedHashMultimap.create<Sentence, WordOccurrence>()
    for (occurrence in wordOccurrences) {
      occurrenceBySentence.put(occurrence.sentence, occurrence)
    }

    val directions = EntityCounter<LanguagePair>()

    for ((left, right) in sentenceTranslations) {
      val leftOccurrences = occurrenceBySentence[left] ?: emptyList<WordOccurrence>()

      if (leftOccurrences.any()) directions.add(LanguagePair(right.language, left.language))
    }

    val table = db.getTable(ContentDbConstants.sentenceLanguages)

    for ((pair, count) in directions.result) {
      table.insert(pair.knownLanguage.code, pair.learnLanguage.code, count)
    }
  }

  private fun writeDifficulties(difficulties: Map<Sentence, Int>, sentenceMapping: Map<Sentence, Long>, db: SqlJetDb) {
    val difficultiesTable = db.getTable(ContentDbConstants.sentenceDifficulty)
    for ((sentence, difficulty) in difficulties) {
      val sentenceId = sentenceMapping[sentence]
      difficultiesTable.insert(sentenceId, difficulty)
    }
  }

  private fun writeWords(wordsOccurrences: List<WordOccurrence>,
                         sentenceMapping: Map<Sentence, Long>,
                         wordCounts: Map<Word, Int>, db: SqlJetDb): Map<WordOccurrence, Long> {
    val mapping = mutableMapOf<WordOccurrence, Long>()
    val wordsTable = db.getTable(ContentDbConstants.words)
    val occurrencesTable = db.getTable(ContentDbConstants.wordOccurrences)
    val countsTable = db.getTable(ContentDbConstants.wordCounts)

    for (occurrencePair in wordsOccurrences.distinct().groupBy { it.word }) {
      val word = occurrencePair.key
      val lang = word.language.code
      val count = wordCounts[word] ?: continue
      val wordId = wordsTable.insert(lang, word.lemma)

      countsTable.insert(wordId, count)

      for (occurrence in occurrencePair.value) {
        val sentenceId = sentenceMapping[occurrence.sentence]
        val occurrenceId = occurrencesTable.insert(wordId, sentenceId, occurrence.startIndex, occurrence.endIndex)
        mapping[occurrence] = occurrenceId
      }
    }
    return mapping
  }

  private fun writeSentences(translations: Map<Sentence, Sentence>, db: SqlJetDb): Map<Sentence, Long> {
    val sentenceIdToRealId = mutableMapOf<Sentence, Long>()

    val sentencesTable = db.getTable(ContentDbConstants.sentences)

    val sentenceTranslations = db.getTable(ContentDbConstants.sentenceTranslations)
    for (pair in translations) {
      val from = getOrInsert(pair.key, sentenceIdToRealId, sentencesTable)
      val to = getOrInsert(pair.value, sentenceIdToRealId, sentencesTable)
      sentenceTranslations.insert(from, to)
    }

    return sentenceIdToRealId
  }

  private fun getOrInsert(sentence: Sentence, sentenceToId: MutableMap<Sentence, Long>, sentencesTable: ISqlJetTable): Long {
    val id = sentenceToId[sentence]
    if (id == null) {
      val insertedId = sentencesTable.insert(sentence.language.code, sentence.text)
      sentenceToId[sentence] = insertedId
      return insertedId
    } else return id
  }

  private fun createDbSchema(db: SqlJetDb) {
    db.createTable("CREATE TABLE ${ContentDbConstants.sentences} (id INTEGER PRIMARY KEY, language TEXT, text TEXT)")
    db.createTable("CREATE TABLE ${ContentDbConstants.sentenceTranslations} (key INTEGER PRIMARY KEY, value INTEGER)")
    db.createTable("CREATE TABLE ${ContentDbConstants.words} (id INTEGER PRIMARY KEY, language TEXT, lemma TEXT)")
    db.createTable("CREATE TABLE ${ContentDbConstants.wordOccurrences} (id INTEGER PRIMARY KEY, wordId INTEGER, sentenceId INTEGER, startIndex INTEGER, endIndex INTEGER)")
    db.createTable("CREATE TABLE ${ContentDbConstants.sentenceDifficulty} (sentenceId INTEGER PRIMARY KEY, difficulty INTEGER)")
    db.createTable("CREATE TABLE ${ContentDbConstants.wordCounts} (wordId INTEGER PRIMARY KEY, count INTEGER)")
    db.createTable("CREATE TABLE ${ContentDbConstants.fillTheGapOccurrences} (form TEXT, topic TEXT, occurrenceId INTEGER)")
    db.createTable("CREATE TABLE ${ContentDbConstants.sentenceLanguages} (knownLanguage TEXT, learnLanguage TEXT, count INTEGER)")
    db.createIndex("CREATE INDEX germanWordOccurrencesIndex ON ${ContentDbConstants.wordOccurrences} (wordId, sentenceId)")
    db.createIndex("CREATE INDEX fillTheGapFormIndex ON ${ContentDbConstants.fillTheGapOccurrences} (form)")
  }
}