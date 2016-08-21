package am5800.harmonie.app.vm

import am5800.common.Language
import am5800.common.Lemma
import am5800.common.utils.Lifetime
import am5800.common.utils.properties.NullableProperty
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.feedback.ErrorReportingService
import am5800.harmonie.app.model.flow.FlowManager
import am5800.harmonie.app.model.lemmasMeaning.LemmaTranslationsProvider
import am5800.harmonie.app.model.localization.LocalizationService
import am5800.harmonie.app.model.parallelSentence.ParallelSentenceFlowManager
import am5800.harmonie.app.model.parallelSentence.SentenceScore
import am5800.harmonie.app.model.repetition.LearnScore
import am5800.harmonie.app.model.sql.KeyValueDatabase
import com.google.common.collect.LinkedHashMultimap
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
                                private val lemmasTranslationsProvider: LemmaTranslationsProvider,
                                keyValueDatabase: KeyValueDatabase,
                                reportingService: ErrorReportingService) : ViewModelBase(lifetime) {
  enum class State {
    ShowQuestion,
    ShowAnswer
  }

  val help = NullableProperty<String>(lifetime, null)

  val problemWords = Property<List<String>>(lifetime, emptyList())

  val reportCommands = IssueReportingMenuHelper.createMenuItems(reportingService, localizationService, lifetime, { describeState() })

  private fun describeState(): String {
    return parallelSentenceFlowManager.question.value!!.question.uid
  }

  val continueBtnText = localizationService.createProperty(lifetime, { it.showTranslation })

  private val state = Property(lifetime, State.ShowQuestion)

  fun next() {
    if (state.value == State.ShowQuestion) {
      state.value = State.ShowAnswer
    } else {
      throw InvalidStateException("Already showing answer")
    }
  }

  fun submit(buttonIndex: Int) {
    if (buttonIndex < 0 || buttonIndex > 2) throw Exception("buttonIndex is out of range: $buttonIndex")

    val scores = LinkedHashMap<Lemma, LearnScore>()
    val vms = question.value.filterIsInstance<ToggleableWordViewModel>()
    for (vm in vms) {
      scores.put(vm.lemma, vm.state.value)
    }

    val sentenceScore = SentenceScore.values()[buttonIndex]

    parallelSentenceFlowManager.submitScore(scores, sentenceScore)
    flowManager.next(scores.count { it.value == LearnScore.Good }, scores.count { it.value == LearnScore.Bad })
  }

  val answerGroupVisibility = Property(lifetime, false)
  val question = Property(lifetime, emptyList<WordViewModel>())
  val answer = Property(lifetime, "")
  val btn0Text = localizationService.createProperty(lifetime, { it.unclear })
  val btn1Text = localizationService.createProperty(lifetime, { it.unclear })
  val btn2Text = localizationService.createProperty(lifetime, { it.clear })

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
      activationRequested.fire(Unit)
      if (keyValueDatabase.getValue("ParallelSentenceQuizHelpShowed", "no") == "no") {
        help.value = localizationService.getCurrentTable().parallelSentencesQuizHelp
        keyValueDatabase.setValue("ParallelSentenceQuizHelpShowed", "yes")
      } else {
        help.value = null
      }
    })
  }

  private fun updateVms(vms: List<ToggleableWordViewModel>) {
    val problems = vms.filter { it.state.value == LearnScore.Bad }.map { Pair(it.lemma, it.text) }

    val learnLanguage = parallelSentenceFlowManager.question.value!!.answer.language

    problemWords.value = presentProblems(learnLanguage, problems)
  }

  private fun presentProblems(knownLanguage: Language, problems: List<Pair<Lemma, String>>): List<String> {
    val lemmasPresentations = LinkedHashMultimap.create<Lemma, String>()
    for ((lemma, text) in problems) {
      lemmasPresentations.put(lemma, text)
    }

    return lemmasPresentations.asMap().map {
      val lemma = it.key
      val presentations = it.value
      val sb = StringBuilder()
      if (presentations.size == 1 && lemma.lemma.equals(presentations.first(), true)) {
        sb.append(presentations.first())
      } else {
        sb.append(presentations.joinToString(", "))
        sb.append("(${lemma.lemma})")
      }
      sb.append(": ")

      val meanings = lemmasTranslationsProvider.getTranslations(lemma, knownLanguage)
      if (meanings.isEmpty()) sb.append("<${localizationService.getCurrentTable().noTranslation}>")
      else sb.append(meanings.joinToString("; "))

      sb.toString()
    }
  }

  private fun subscribeToVms(vms: List<WordViewModel>, lt: Lifetime) {
    val tvms = vms.filterIsInstance<ToggleableWordViewModel>()
    for (tvm in tvms) {
      tvm.state.onChange(lt, { updateVms(tvms) })
    }
  }
}