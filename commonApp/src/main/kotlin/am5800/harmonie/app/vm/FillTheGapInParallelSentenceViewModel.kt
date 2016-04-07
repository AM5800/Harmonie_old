package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.Signal
import am5800.common.utils.functions.shuffle
import am5800.harmonie.app.model.flow.FillTheGapInParallelSentenceFlowItemManager
import am5800.harmonie.app.model.flow.FillTheGapInParallelSentenceQuestion
import am5800.harmonie.app.model.flow.FlowManager

class VariantButtonViewModel(val title: String, enabled: Boolean, lifetime: Lifetime) {
  val enabled = Property(lifetime, enabled)
  val signal = Signal<Unit>(lifetime)
}

class FillTheGapInParallelSentenceViewModel(
    lifetime: Lifetime,
    managers: Collection<FillTheGapInParallelSentenceFlowItemManager>,
    private val flowManager: FlowManager) : ViewModelBase(lifetime) {

  private enum class State {
    ShowQuestion, ShowAnswer
  }

  val sentence = Property(lifetime, "")
  val translation = Property(lifetime, "")
  val variants = Property<List<VariantButtonViewModel>>(lifetime, emptyList())
  val variantsVisible = Property(lifetime, true)
  val continueVisible = Property(lifetime, false)

  private val state = Property(lifetime, State.ShowQuestion)

  init {
    state.onChangeNotNull(lifetime, {
      if (it == State.ShowQuestion) {
        variantsVisible.value = true
        continueVisible.value = false
      } else {
        variantsVisible.value = false
        continueVisible.value = true
      }
    })


    for (manager in managers) {
      manager.question.forEachValue(lifetime, { question, lt ->
        question!!
        state.value = State.ShowQuestion
        sentence.value = prepareQuestion(question)
        translation.value = question.translation.text
        buildVariants(question, lt)
        state.onValue(lt, State.ShowAnswer, { sentence.value = question.sentence.text })

        activationRequested.fire(Unit)
      })
    }
  }

  private fun buildVariants(question: FillTheGapInParallelSentenceQuestion, lifetime: Lifetime) {
    val wrongs = question.wrongVariants.map {
      val vm = VariantButtonViewModel(it, true, lifetime)
      vm.signal.subscribe(lifetime, {
        vm.enabled.value = false
      })
      vm
    }

    val correct = VariantButtonViewModel(question.correctAnswer, true, lifetime)
    correct.signal.subscribe(lifetime, {
      state.value = State.ShowAnswer
    })

    variants.value = wrongs.plus(correct).shuffle(null)
  }

  private fun prepareQuestion(question: FillTheGapInParallelSentenceQuestion): String {
    val text = question.sentence.text
    val firstPart = text.substring(0, question.occurrenceStart)
    val secondPart = text.substring(question.occurrenceEnd)
    return firstPart + " <?> " + secondPart
  }

  fun next() {
    flowManager.next()
  }
}