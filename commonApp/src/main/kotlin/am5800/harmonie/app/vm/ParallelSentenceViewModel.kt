package am5800.harmonie.app.vm

import am5800.common.db.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.app.model.dbAccess.AttemptScore
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.flow.ParallelSentenceFlowManager
import am5800.harmonie.app.model.flow.ParallelSentenceQuestion
import java.util.*

open class WordViewModel(val text: String)

class ToggleableWordViewModel(val word: Word, text: String, val state: Property<AttemptScore>) : WordViewModel(text) {
  fun toggle() {
    if (state.value == AttemptScore.Ok) state.value = AttemptScore.Wrong
    else state.value = AttemptScore.Ok
  }
}

class ParallelSentenceViewModel(lifetime: Lifetime,
                                private val parallelSentenceFlowManager: ParallelSentenceFlowManager,
                                private val flowManager: FlowManager) : ViewModel by ViewModelBase(lifetime) {
  private enum class State {
    ShowQuestion,
    ShowAnswer
  }

  private val state = Property(lifetime, State.ShowQuestion)


  fun next() {
    if (state.value == State.ShowQuestion) {
      state.value = State.ShowAnswer
    } else {
      val scores = LinkedHashMap<Word, AttemptScore>()
      val vms = question.value?.filterIsInstance<ToggleableWordViewModel>() ?: emptyList()
      for (vm in vms) {
        scores.put(vm.word, vm.state.value!!)
      }
      parallelSentenceFlowManager.submitScore(scores)
      flowManager.next()
    }
  }

  val answerGroupVisibility = Property(lifetime, Visibility.Collapsed)
  val question = Property(lifetime, emptyList<WordViewModel>())
  val answer = Property(lifetime, "")

  init {
    state.forEachValue(lifetime, { state, lt ->
      if (state == State.ShowQuestion) {
        answerGroupVisibility.value = Visibility.Collapsed
      } else if (state == State.ShowAnswer) {
        answerGroupVisibility.value = Visibility.Visible
      }
    })

    parallelSentenceFlowManager.question.forEachValue(lifetime, { data, lt ->
      data!!
      state.value = State.ShowQuestion
      question.value = prepareVms(data, lifetime)
      answer.value = data.answer.text
      activationRequired.fire(Unit)
    })
  }

  private fun prepareVms(data: ParallelSentenceQuestion, lifetime: Lifetime): List<WordViewModel> {
    val result = mutableListOf<WordViewModel>()
    val properties = data.lemmas.keySet().map { Pair(it, Property(lifetime, AttemptScore.Ok)) }.toMap()
    val sortedOccurrences = data.lemmas.asMap()
        .flatMap { pair -> pair.value.map { Pair(pair.key, it) } }
        .sortedBy { it.second.start }

    val sentence = data.question.text
    var index = 0
    for ((word, range) in sortedOccurrences) {
      if (index != range.start) {
        val substringSinceLastProcessedWord = sentence.substring(index, range.start)
        val words = substringSinceLastProcessedWord.split(' ').map { it.trim() }.filterNot { it.isBlank() }
        result.addAll(words.map { WordViewModel(it) })
      }
      val text = sentence.substring(range.start, range.end)

      result.add(ToggleableWordViewModel(word, text, properties[word]!!))

      index = range.end + 1
    }

    return result
  }
}