package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.common.utils.Signal
import am5800.common.utils.functions.shuffle
import am5800.harmonie.app.model.features.feedback.ErrorReportingService
import am5800.harmonie.app.model.features.fillTheGap.FillTheGapFlowItemManager
import am5800.harmonie.app.model.features.fillTheGap.FillTheGapQuestion
import am5800.harmonie.app.model.features.flow.FlowManager
import am5800.harmonie.app.model.features.localization.LocalizationService

class VariantButtonViewModel(val title: String, enabled: Boolean, lifetime: Lifetime) {
  val enabled = Property(lifetime, enabled)
  val signal = Signal<Unit>(lifetime)
}

class FillTheGapViewModel(
    lifetime: Lifetime,
    managers: Collection<FillTheGapFlowItemManager>,
    private val flowManager: FlowManager,
    reportingService: ErrorReportingService,
    localizationService: LocalizationService) : ViewModelBase(lifetime) {

  val reportCommands = IssueReportingMenuHelper.createMenuItems(reportingService, localizationService, lifetime, { describeState() })

  private fun describeState(): String {
    return sentence.value!! + "/" + translation.value!! + "/" + variants.value!!.joinToString(";") { it.title }
  }

  val sentence = Property(lifetime, "")
  val translation = Property(lifetime, "")
  val variants = Property<List<VariantButtonViewModel>>(lifetime, emptyList())
  val variantsVisible = Property(lifetime, true)

  init {
    for (manager in managers) {
      manager.question.forEachValue(lifetime, { question, lt ->
        question!!
        sentence.value = prepareQuestion(question)
        translation.value = question.translation.text
        buildVariants(question, lt)

        activationRequested.fire(Unit)
      })
    }
  }

  private fun buildVariants(question: FillTheGapQuestion, lifetime: Lifetime) {
    val wrongs = question.wrongVariants.map {
      val vm = VariantButtonViewModel(it, true, lifetime)
      vm.signal.subscribe(lifetime, {
        vm.enabled.value = false
      })
      vm
    }

    val correct = VariantButtonViewModel(question.correctAnswer, true, lifetime)
    correct.signal.subscribe(lifetime, {
      flowManager.next()
    })

    variants.value = wrongs.plus(correct).shuffle(null)
  }

  private fun prepareQuestion(question: FillTheGapQuestion): String {
    val text = question.sentence.text
    val firstPart = text.substring(0, question.occurrenceStart)
    val secondPart = text.substring(question.occurrenceEnd)
    return firstPart + " <?> " + secondPart
  }
}