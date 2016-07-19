package am5800.harmonie.app.vm

import am5800.common.Word
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.NullableProperty
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.features.feedback.ErrorReportingService
import am5800.harmonie.app.model.features.localization.LocalizationService
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.features.repetition.LearnScore
import am5800.harmonie.app.model.services.KeyValueDatabase
import am5800.harmonie.app.model.services.flow.FlowManager
import java.util.*

open class WordViewModel(val text: String, val needSpaceBefore: Boolean)

class ToggleableWordViewModel(val word: Word, text: String,
                              val state: Property<LearnScore>,
                              needSpaceBefore: Boolean) : WordViewModel(text, needSpaceBefore) {
  fun toggle() {
    if (state.value == LearnScore.Good) state.value = LearnScore.Bad
    else state.value = LearnScore.Good
  }
}

class ParallelSentenceViewModel(lifetime: Lifetime,
                                private val parallelSentenceFlowManager: ParallelSentenceFlowManager,
                                private val flowManager: FlowManager,
                                localizationService: LocalizationService,
                                keyValueDatabase: KeyValueDatabase,
                                reportingService: ErrorReportingService) : ViewModelBase(lifetime) {
  enum class State {
    ShowQuestion,
    ShowAnswer
  }

  val help = NullableProperty<String>(lifetime, null)

  val reportCommands = IssueReportingMenuHelper.createMenuItems(reportingService, localizationService, lifetime, { describeState() })

  private fun describeState(): String {
    val pair = parallelSentenceFlowManager.question.value!!
    return pair.question.text + "/" + pair.answer.text
  }

  val continueBtnText = localizationService.createProperty(lifetime, { it.continueButton })

  private val state = Property(lifetime, State.ShowQuestion)


  fun next() {
    if (state.value == State.ShowQuestion) {
      state.value = State.ShowAnswer
    } else {
      val scores = LinkedHashMap<Word, LearnScore>()
      val vms = question.value.filterIsInstance<ToggleableWordViewModel>()
      for (vm in vms) {
        scores.put(vm.word, vm.state.value)
      }
      parallelSentenceFlowManager.submitScore(scores)
      flowManager.next(scores.count { it.value == LearnScore.Good }, scores.count { it.value == LearnScore.Bad })
    }
  }

  val answerGroupVisibility = Property(lifetime, false)
  val question = Property(lifetime, emptyList<WordViewModel>())
  val answer = Property(lifetime, "")

  init {
    state.forEachValue(lifetime, { state, lt ->
      if (state == State.ShowQuestion) {
        answerGroupVisibility.value = false
      } else if (state == State.ShowAnswer) {
        answerGroupVisibility.value = true
      }
    })

    parallelSentenceFlowManager.question.forEachValue(lifetime, { data, lt ->
      if (data == null) return@forEachValue
      state.value = State.ShowQuestion
      question.value = createViewModelsForQuestion(data, lifetime)
      answer.value = data.answer.text
      activationRequested.fire(Unit)
      if (keyValueDatabase.getValue("ParallelSentenceQuizHelpShowed", "no") == "no") {
        help.value = localizationService.getCurrentTable().parallelSentencesQuizHelp
        keyValueDatabase.setValue("ParallelSentenceQuizHelpShowed", "yes")
      } else {
        help.value = null
      }
    })
  }
}