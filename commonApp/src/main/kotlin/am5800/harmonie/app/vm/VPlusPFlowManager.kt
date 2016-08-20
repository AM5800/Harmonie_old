package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.functions.shuffle
import am5800.common.utils.properties.Property
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.exercises.vplusp.VPlusPData
import am5800.harmonie.app.model.exercises.vplusp.VPlusPFlowManager
import am5800.harmonie.app.model.flow.FlowManager

class VPlusPViewModel(private val vPlusPFlowManager: VPlusPFlowManager,
                      private val flowManager: FlowManager,
                      private val debugOptions: DebugOptions,
                      lifetime: Lifetime) : ViewModelBase(lifetime) {

  private var expectedAnswer = ""
  private var attempts = 0

  val question = Property(lifetime, "")
  val button0Title = Property(lifetime, "")
  val button1Title = Property(lifetime, "")
  val button2Title = Property(lifetime, "")

  val button0Enabled = Property(lifetime, true)
  val button1Enabled = Property(lifetime, true)
  val button2Enabled = Property(lifetime, true)

  init {
    vPlusPFlowManager.currentItem.forEachValue(lifetime, { data, lt ->
      if (data == null) return@forEachValue
      val prepositions = vPlusPFlowManager.getKnownPrepositions()
      val answer = data.preposition
      expectedAnswer = answer

      val variants = listOf(answer).plus(prepositions.filter { it != answer }).take(3).shuffle(debugOptions.random)
      button0Title.value = variants[0]
      button1Title.value = variants[1]
      button2Title.value = variants[2]

      button0Enabled.value = true
      button1Enabled.value = true
      button2Enabled.value = true

      attempts = 0

      question.value = mkQuestionText(data)
      activationRequested.fire(Unit)
    })
  }

  private fun mkQuestionText(data: VPlusPData): String {
    return data.sentence.text.substring(0, data.occurrenceStart) + "___" + data.sentence.text.substring(data.occurrenceEnd)
  }

  fun submit(buttonIndex: Int) {
    val titles = listOf(button0Title.value, button1Title.value, button2Title.value)
    val correct = titles[buttonIndex] == expectedAnswer

    if (correct) {
      vPlusPFlowManager.submitResult(attempts == 0)
      flowManager.next(1, 0)
    } else {
      ++attempts
      val button = listOf(button0Enabled, button1Enabled, button2Enabled)[buttonIndex]
      button.value = false
    }
  }
}