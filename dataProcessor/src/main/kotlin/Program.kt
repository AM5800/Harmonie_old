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
  val languages = extractLanguages(data)

  val translations = mutableMapOf<Sentence, Sentence>()
  val oldOccurrences = data.wordOccurrences.groupBy { it.sentence }
  val wordOccurrences = mutableListOf<WordOccurrence>()
  val difficulties = mutableMapOf<Sentence, Int>()

  for (language in languages) {
    for ((sentence, difficultyLevel) in filterByDifficulty(data, language)) {
      val translated = data.sentenceTranslations[sentence]!!
      wordOccurrences.addAll(oldOccurrences[sentence] ?: continue)

      translations.put(sentence, translated)
      translations.put(translated, sentence)
      difficulties.put(sentence, difficultyLevel)
    }
  }

  return Data(translations, wordOccurrences, difficulties)
}

fun extractLanguages(data: Data): List<Language> {
  return data.wordOccurrences.groupBy { it.sentence.language }.keys.toList()
}

fun filterByDifficulty(data: Data, language: Language): Map<Sentence, Int> {
  val sentences = data.sentenceTranslations.map { if (it.key.language == language) it.key else it.value }.toList()
  val occurrences = data.wordOccurrences.filter { it.word.language == language }
  val sentenceToOccurrences: Map<Sentence, List<WordOccurrence>> = occurrences.groupBy { it.sentence }
  val wordToOccurrences = occurrences.groupBy { it.word }
  val wordFrequencies = wordToOccurrences.mapValues { it.value.count() / data.wordOccurrences.size.toDouble() }

  val sentencesWithDifficulties = sentences
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


