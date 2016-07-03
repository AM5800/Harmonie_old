package am5800.harmonie.app.model.services.learnGraph

import am5800.common.Language
import am5800.common.LearnGraphNode
import am5800.harmonie.app.model.services.*
import am5800.harmonie.app.model.services.sentencesAndWords.SqlSentenceAndWordsProvider

class SqlLearnGraphLoader(private val contentDb: ContentDb, private val sentenceAndWordsProvider: SqlSentenceAndWordsProvider) : LearnGraphLoader {
  override fun load(learnLanguage: Language): List<LearnGraphNode> {
    val query = """
      SELECT words.id, words.lemma, learnGraph.sentenceIds FROM words
        INNER JOIN learnGraph
          ON learnGraph.wordId = words.id
        WHERE words.language='${learnLanguage.code}'
        ORDER BY learnGraph.wordOrder
    """

    val queryResult = contentDb.query3<Long, String, String>(query)
    return createUnlockInfos(queryResult, learnLanguage)
  }

  private fun createUnlockInfos(queryResult: List<Tuple3<Long, String, String>>, learnLanguage: Language): List<LearnGraphNode> {
    val sentences = sentenceAndWordsProvider.getAllSentences(learnLanguage).map { Pair(it.id, it) }.toMap()
    return queryResult.map {
      val word = SqlWord(it.value1, learnLanguage, it.value2)
      val unlockedSentences = parseUnlockedSentences(it.value3, sentences)
      LearnGraphNode(word, unlockedSentences)
    }
  }

  private fun parseUnlockedSentences(string: String?, sentences: Map<Long, SqlSentence>): List<SqlSentence> {
    // there is String.IsNullOrEmpty function. But smart cast won't work with it
    if (string == null || string.isEmpty()) return emptyList()

    val ids = string.split(';').map { it.toLong() }.filterNotNull()
    return ids.map { sentences[it] }.filterNotNull()
  }
}