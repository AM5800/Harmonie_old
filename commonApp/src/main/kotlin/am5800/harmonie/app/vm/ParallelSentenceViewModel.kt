package am5800.harmonie.app.vm

import am5800.common.Lemma
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.NullableProperty
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.features.feedback.ErrorReportingService
import am5800.harmonie.app.model.features.localization.LocalizationService
import am5800.harmonie.app.model.features.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.features.parallelSentence.SentenceScore
import am5800.harmonie.app.model.features.repetition.LearnScore
import am5800.harmonie.app.model.services.KeyValueDatabase
import am5800.harmonie.app.model.services.flow.FlowManager
import sun.plugin.dom.exception.InvalidStateException
import java.util.*

open class WordViewModel(val text: String, val needSpaceAfter: Boolean)

class ToggleableWordViewModel(val lemma: Lemma, text: String,
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
                                private val localizationService: LocalizationService,
                                keyValueDatabase: KeyValueDatabase,
                                reportingService: ErrorReportingService) : ViewModelBase(lifetime) {
  enum class State {
    ShowQuestion,
    ShowAnswer
  }

  val help = NullableProperty<String>(lifetime, null)

  val reportCommands = IssueReportingMenuHelper.createMenuItems(reportingService, localizationService, lifetime, { describeState() })

  private fun describeState(): String {
    return parallelSentenceFlowManager.question.value!!.question.uid
  }

  val continueBtnText = localizationService.createProperty(lifetime, { it.showTranslation })

  private val state = Property(lifetime, State.ShowQuestion)
  private var currentScoreIsGood = true


  fun next() {
    if (state.value == State.ShowQuestion) {
      state.value = State.ShowAnswer
    } else {
      throw InvalidStateException("Already showing answer")
    }
  }

  fun submit(buttonIndex: Int) {
    if (buttonIndex < 1 || buttonIndex > 3) throw Exception("buttonIndex is out of range: $buttonIndex")

    val scores = LinkedHashMap<Lemma, LearnScore>()
    val vms = question.value.filterIsInstance<ToggleableWordViewModel>()
    for (vm in vms) {
      scores.put(vm.lemma, vm.state.value)
    }

    val sentenceScore = SentenceScore.values()[buttonIndex - 1 + if (currentScoreIsGood) 1 else 0]

    parallelSentenceFlowManager.submitScore(scores, sentenceScore)
    flowManager.next(scores.count { it.value == LearnScore.Good }, scores.count { it.value == LearnScore.Bad })
  }

  val answerGroupVisibility = Property(lifetime, false)
  val question = Property(lifetime, emptyList<WordViewModel>())
  val answer = Property(lifetime, "")
  val score1Text = Property(lifetime, "")
  val score2Text = Property(lifetime, "")
  val score3Text = Property(lifetime, "")

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
      val vms = createViewModelsForQuestion(data, lt)
      subscribeToVms(vms, lt)
      question.value = vms
      answer.value = data.answer.text
      onGoodScore()
      activationRequested.fire(Unit)
      if (keyValueDatabase.getValue("ParallelSentenceQuizHelpShowed", "no") == "no") {
        help.value = localizationService.getCurrentTable().parallelSentencesQuizHelp
        keyValueDatabase.setValue("ParallelSentenceQuizHelpShowed", "yes")
      } else {
        help.value = null
      }
    })
  }

  private fun updateScore(vms: List<ToggleableWordViewModel>) {
    val good = vms.count { it.state.value == LearnScore.Good }
    if (good.toDouble() / vms.size > 0.8) onGoodScore()
    else onBadScore()
  }

  private fun subscribeToVms(vms: List<WordViewModel>, lt: Lifetime) {
    val tvms = vms.filterIsInstance<ToggleableWordViewModel>()
    for (tvm in tvms) {
      tvm.state.onChange(lt, { updateScore(tvms) })
    }
  }

  private fun onGoodScore() {
    currentScoreIsGood = true
    val table = localizationService.getCurrentTable()
    score1Text.value = table.unclear
    score2Text.value = table.uncertain
    score3Text.value = table.clear
  }

  private fun onBadScore() {
    currentScoreIsGood = false
    val table = localizationService.getCurrentTable()
    score1Text.value = table.blackout
    score2Text.value = table.unclear
    score3Text.value = table.uncertain
  }
}