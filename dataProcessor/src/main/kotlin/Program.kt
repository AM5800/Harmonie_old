import am5800.common.Language
import am5800.common.db.Sentence
import am5800.common.db.WordOccurrence
import corpus.CorpusRepository
import java.io.File

fun main(args: Array<String>) {
  val repository = CorpusRepository(File("data/corpuses"))
  val data = prepareData(repository)
  val filteredData = filterData(data)
  DbWriter().write(File("androidApp/src/main/assets/content.db"), filteredData)
}

fun prepareData(repository: CorpusRepository): Data {
  val infos = repository.getCorpuses().filter { it.formatId.equals("harmonie", true) }

  val parser = HarmonieParallelSentencesParser()

  val initial: Data? = null
  return infos.fold(initial, { acc, info ->
    val data = parser.parse(info)
    if (acc == null) return data
    else mergeData(acc, data)
  })!!
}

fun mergeData(left: Data, right: Data): Data {
  val occurrences = left.wordOccurrences.plus(right.wordOccurrences).distinct()
  val translations = left.sentenceTranslations.plus(right.sentenceTranslations)

  return Data(translations, occurrences, emptyMap())
}

fun filterData(data: Data): Data {
  val difficulty = filterByDifficulty(data)
  val translations = mutableMapOf<Sentence, Sentence>()

  val oldOccurrences = data.wordOccurrences.groupBy { it.sentence }
  val wordOccurrences = mutableListOf<WordOccurrence>()

  for (sentence in difficulty.keys) {
    val translated = data.sentenceTranslations[sentence]!!

    translations.put(sentence, translated)
    translations.put(translated, sentence)

    wordOccurrences.addAll(oldOccurrences[sentence] ?: continue)
  }

  return Data(translations, wordOccurrences, difficulty)
}

fun filterByDifficulty(data: Data): Map<Sentence, Int> {
  val germanSentences = data.sentenceTranslations.map { if (it.key.language == Language.German) it.key else it.value }.toList()
  val germanOccurrences = data.wordOccurrences.filter { it.word.language == Language.German }
  val sentenceToOccurrences: Map<Sentence, List<WordOccurrence>> = germanOccurrences.groupBy { it.sentence }
  val wordToOccurrences = germanOccurrences.groupBy { it.word }
  val wordFrequencies = wordToOccurrences.mapValues { it.value.count() / data.wordOccurrences.size.toDouble() }

  val sentencesWithDifficulties = germanSentences
      .map {
        Pair(it, sentenceToOccurrences[it] ?: emptyList())
      }
      .toMap()
      .mapValues { it.value.map { occ -> wordFrequencies[occ.word] ?: 0.0 } }
      .filter { it.value.size > 4 && it.value.size < 15 }
      .mapValues { it.value.fold(1.0, { i, d -> i * d }) }
      .toList()
      .sortedByDescending { it.second }
      .take(10000)


  val actualCount = sentencesWithDifficulties.size
  val requiredBuckets = 100

  return sentencesWithDifficulties.mapIndexed { i, pair -> Pair(pair.first, i / (actualCount / requiredBuckets)) }
      .toMap()
}


