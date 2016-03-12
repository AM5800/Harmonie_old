import am5800.common.Language
import am5800.common.db.Sentence
import am5800.common.db.Word
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
  val infos = repository.getCorpuses().filter { it.formatId.equals("parallel", true) }

  val translations = mutableMapOf<Sentence, Sentence>()
  val wordOccurrences = mutableListOf<WordOccurrence>()

  val parser = LetsmtCorpusParser()
  for (corpus in infos) {
    val enPath = File(corpus.infoFile.parentFile, corpus.metadata["en"])
    val dePath = File(corpus.infoFile.parentFile, corpus.metadata["de"])

    val enData = parser.parse(enPath)
    val deData = parser.parse(dePath)

    for (deSentencePair in deData.sentences) {
      val enSentence = enData.sentences[deSentencePair.key] ?: continue

      val englishSentence = Sentence(Language.English, enSentence.trim())
      val germanSentence = Sentence(Language.German, deSentencePair.value.trim())

      wordOccurrences.addAll(processWords(englishSentence, enData.words[deSentencePair.key]))
      wordOccurrences.addAll(processWords(germanSentence, deData.words[deSentencePair.key]))

      translations.put(englishSentence, germanSentence)
      translations.put(germanSentence, englishSentence)
    }
  }

  return Data(translations, wordOccurrences.distinct(), emptyMap())
}

fun processWords(sentence: Sentence, occurrences: Set<ParseWordOccurrence>): List<WordOccurrence> {
  return occurrences.map {
    val lemma = it.lemma
    val word = Word(sentence.language, lemma)
    WordOccurrence(word, sentence, it.sentenceStartIndex, it.sentenceEndIndex)
  }
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

  return germanSentences
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
      .mapIndexed { i, pair -> Pair(pair.first, i) }
      .toMap()
}


