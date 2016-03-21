package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.repetition.AttemptScore
import am5800.harmonie.app.model.flow.ParallelSentenceQuestion

fun createViewModelsForQuestion(data: ParallelSentenceQuestion, lifetime: Lifetime): List<WordViewModel> {
  val result = mutableListOf<WordViewModel>()
  val properties = data.occurrences.keySet().map { Pair(it, Property(lifetime, AttemptScore.Ok)) }.toMap()
  val sortedOccurrences = data.occurrences.asMap()
      .flatMap { pair -> pair.value.map { Pair(pair.key, it) } }
      .sortedBy { it.second.start }

  val sentence = data.question.text
  var index = 0
  for ((word, range) in sortedOccurrences) {
    if (index != range.start) {
      processNonToggleables(result, sentence, index, range.start)
    }
    val text = sentence.substring(range.start, range.end)

    val needSpaceBefore = if (range.start == 0) false else sentence[range.start - 1] == ' '
    result.add(ToggleableWordViewModel(word, text, properties[word]!!, needSpaceBefore, data.highlightedWords.contains(word)))

    index = range.end
  }

  if (index != sentence.length) {
    processNonToggleables(result, sentence, index, sentence.length)
  }

  return result
}

private fun processNonToggleables(result: MutableList<WordViewModel>, sentence: String, startIndex: Int, endIndex: Int) {
  val substr = sentence.substring(startIndex, endIndex)
  val words = substr.split(' ').map { it.trim() }.filterNot { it.isBlank() }
  val needSpaceBeforeFirst = if (startIndex == 0) false else sentence[startIndex - 1] == ' ' || sentence[startIndex] == ' '

  words.forEachIndexed { i, s ->
    val needSpaceBefore = if (i == 0) needSpaceBeforeFirst else true
    result.add(WordViewModel(s, needSpaceBefore))
  }
}

