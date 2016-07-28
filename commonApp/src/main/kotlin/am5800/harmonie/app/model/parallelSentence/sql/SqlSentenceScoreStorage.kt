package am5800.harmonie.app.model.parallelSentence.sql

import am5800.common.Sentence
import am5800.harmonie.app.model.UserDb
import am5800.harmonie.app.model.parallelSentence.SentenceScore
import am5800.harmonie.app.model.parallelSentence.SentenceScoreStorage
import am5800.harmonie.app.model.query2


class SqlSentenceScoreStorage(val userDb: UserDb) : SentenceScoreStorage {

  private fun ensureTableCreated() {
    userDb.execute("CREATE TABLE IF NOT EXISTS sentenceScores (uid TEXT PRIMARY KEY, score TEXT)")
  }

  override fun getScores(sentences: List<Sentence>): List<Pair<Sentence, SentenceScore?>> {
    if (sentences.isEmpty()) return emptyList()

    ensureTableCreated()
    val ids = sentences.map { it.uid }.joinToString(", ") { "'$it'" }
    val query = """
      SELECT uid, score FROM sentenceScores
        WHERE uid IN ($ids)
    """
    val queryResult = userDb.query2<String, String>(query)
        .map { Pair(it.first, SentenceScore.valueOf(it.second)) }
        .toMap()

    return sentences.map { Pair(it, queryResult[it.uid]) }
  }

  override fun setScore(sentence: Sentence, score: SentenceScore) {
    ensureTableCreated()
    val id = sentence.uid
    userDb.execute("INSERT OR REPLACE INTO sentenceScores VALUES ('$id', '$score')")
  }
}