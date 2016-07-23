package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceQuestion
import am5800.harmonie.app.model.features.repetition.LearnScore

fun createViewModelsForQuestion(data: ParallelSentenceQuestion, lifetime: Lifetime): List<WordViewModel> {
  val result = mutableListOf<WordViewModel>()
  val properties = data.occurrences.keySet().map { Pair(it, Property(lifetime, LearnScore.Good)) }.toMap()
  val sortedByStartOccurrences = data.occurrences.asMap()
      .flatMap { pair -> pair.value.map { Pair(pair.key, it) } }
      .sortedBy { it.second.start }

  val sentence = data.question.text
  var index = 0
  for ((word, range) in sortedByStartOccurrences) {
    if (index != range.start) {
      createPlainVms(result, sentence, index, range.start)
    }
    val text = sentence.substring(range.start, range.end)

    val needSpaceAfter = checkIfSpaceAfterNeeded(sentence, range.end)
    result.add(ToggleableWordViewModel(word, text, properties[word]!!, needSpaceAfter))

    index = range.end
  }

  if (index != sentence.length) {
    createPlainVms(result, sentence, index, sentence.length)
  }

  return result
}

private fun checkIfSpaceAfterNeeded(sentence: String, index: Int): Boolean {
  if (index >= sentence.length - 1) return false
  return sentence[index].isWhitespace()
}

private fun createPlainVms(result: MutableList<WordViewModel>, sentence: String, startIndex: Int, endIndex: Int) {
  val substring = sentence.substring(startIndex, endIndex)
  val words = substring.split(' ').map { it.trim() }.filterNot { it.isBlank() }

  words.forEachIndexed { i, s ->
    val needSpaceAfter = if (s == words.last()) checkIfSpaceAfterNeeded(sentence, endIndex - 1) else true
    result.add(WordViewModel(s, needSpaceAfter))
  }
}

