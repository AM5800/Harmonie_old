package am5800.harmonie.app.model.features.parallelSentence.sql

import am5800.common.Sentence
import am5800.harmonie.app.model.features.parallelSentence.SentenceScore
import am5800.harmonie.app.model.features.parallelSentence.SentenceScoreStorage
import am5800.harmonie.app.model.services.UserDb
import am5800.harmonie.app.model.services.query2


class SqlSentenceScoreStorage(val userDb: UserDb) : SentenceScoreStorage {

  private fun ensureTableCreated() {
    userDb.execute("CREATE TABLE IF NOT EXISTS sentenceScores (uid TEXT PRIMARY KEY, score TEXT")
  }

  override fun getScores(sentences: List<Sentence>): List<Pair<Sentence, SentenceScore?>> {
    ensureTableCreated()
    val ids = sentences.map { it.uid }
    val query = """
      SELECT uid, score FROM sentenceScores
        WHERE id IN ($ids)
    """
    val queryResult = userDb.query2<String, String>(query)
        .map { Pair(it.first, SentenceScore.valueOf(it.second)) }
        .toMap()

    return sentences.map { Pair(it, queryResult[it.uid]) }
  }

  override fun setScore(sentence: Sentence, score: SentenceScore) {
    ensureTableCreated()
    val id = sentence.uid
    userDb.execute("INSERT OR REPLACE INTO sentenceScores VALUES ('$id', '$score'")
  }
}