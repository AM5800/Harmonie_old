package am5800.harmonie.app.model.exercises.vplusp

import am5800.harmonie.app.model.sentencesAndLemmas.SqlSentenceAndLemmasProvider
import am5800.harmonie.app.model.sql.ContentDb
import am5800.harmonie.app.model.sql.query1
import am5800.harmonie.app.model.sql.query3

class SqlVPlusPDataProvider(private val contentDb: ContentDb,
                            private val sentenceAndLemmasProvider: SqlSentenceAndLemmasProvider) : VPlusPDataProvider {
  override fun getAllTopics(): List<String> {
    val query = "SELECT DISTINCT(topic) FROM vplusp"

    return contentDb.query1<String>(query)
  }

  override fun get(topic: String): List<VPlusPData> {
    val query = """
      SELECT lemmaOccurrences.sentenceId, lemmaOccurrences.startIndex, lemmaOccurrences.endIndex FROM vplusp
        INNER JOIN lemmaOccurrences
          ON vplusp.occurrenceId = lemmaOccurrences.id
        WHERE vplusp.topic = "$topic"
      """

    val queryResult = contentDb.query3<Long, Int, Int>(query)

    val sentences = sentenceAndLemmasProvider.getSentencesFlat(queryResult.map { it.value1 }).map { Pair(it.sqlId, it) }.toMap()

    return queryResult.map { VPlusPData(sentences[it.value1]!!, it.value2, it.value3, topic) }
  }
}