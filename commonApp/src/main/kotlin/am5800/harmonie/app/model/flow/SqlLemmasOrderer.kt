package am5800.harmonie.app.model.flow

import am5800.common.Lemma
import am5800.harmonie.app.model.sentencesAndLemmas.SentenceAndLemmasProvider
import am5800.harmonie.app.model.sql.UserDb
import am5800.harmonie.app.model.sql.query1

class SqlLemmasOrderer(private val userDb: UserDb, sentenceAndLemmasProvider: SentenceAndLemmasProvider) : LemmasOrderer {

  private var lemmasOrder = loadLemmas(userDb, sentenceAndLemmasProvider)

  private fun loadLemmas(userDb: UserDb, sentenceAndLemmasProvider: SentenceAndLemmasProvider): MutableMap<Lemma, Int> {
    ensureTableCreated(userDb)

    val query = """
      SELECT lemmaId FROM lemmasOrder ORDER BY id
    """

    val queryResult = userDb.query1<String>(query)
    val result = mutableMapOf<Lemma, Int>()
    autoincrement = 0
    for (lemma in sentenceAndLemmasProvider.getLemmasByIds(queryResult)) {
      result[lemma] = autoincrement++
    }
    return result
  }

  private fun ensureTableCreated(userDb: UserDb) {
    userDb.execute("CREATE TABLE IF NOT EXISTS lemmasOrder (id INTEGER PRIMARY KEY AUTOINCREMENT, lemmaId STRING)")
  }

  private var autoincrement = 0

  override fun pullUp(lemma: Lemma) {
    lemmasOrder[lemma] = autoincrement++
    save()
  }

  private fun save() {
    userDb.execute("DROP TABLE IF EXISTS lemmasOrder")
    ensureTableCreated(userDb)
    for ((lemma, index) in lemmasOrder.toList().sortedBy { it.second }) {
      userDb.execute("INSERT INTO lemmasOrder(lemmaId) VALUES('${lemma.id}')")
    }
  }

  override fun reorder(lemmas: List<Lemma>): List<Lemma> {
    val top = mutableListOf<Pair<Lemma, Int>>()
    val result = mutableListOf<Lemma>()

    for (lemma in lemmas) {
      val order = lemmasOrder[lemma]
      if (order != null) {
        top.add(Pair(lemma, order))
      } else {
        result.add(lemma)
      }
    }

    top.sortByDescending { it.second }
    return top.map { it.first }.plus(result)
  }
}