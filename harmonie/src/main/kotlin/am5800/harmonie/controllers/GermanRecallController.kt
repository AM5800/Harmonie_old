package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.controllers.defaultControls.ButtonController
import am5800.harmonie.controllers.defaultControls.TextViewController
import am5800.harmonie.model.FlowItemResult
import am5800.harmonie.model.Gender
import am5800.harmonie.model.GermanWordId
import am5800.harmonie.model.RenderedExample
import am5800.harmonie.model.util.Property
import am5800.harmonie.viewBinding.ReflectionBindableController
import android.text.Html


class GermanRecallController(private val example: RenderedExample, markError: () -> Unit) : ReflectionBindableController(R.layout.word_recall_view), FlowItemController {
  override val result: Property<FlowItemResult> = Property(null)
  val dasController: ButtonController = ButtonController(R.id.dasBtn, { submitGender(Gender.Neuter) }, "das")
  val derController: ButtonController = ButtonController(R.id.derBtn, { submitGender(Gender.Masculine) }, "der")
  val dieController: ButtonController = ButtonController(R.id.dieBtn, { submitGender(Gender.Feminine) }, "die")
  val rightController: ButtonController = ButtonController(R.id.rightBtn, { submitAnswer(true) }, "�����")
  val wrongController: ButtonController = ButtonController(R.id.wrongBtn, { submitAnswer(false) }, "�� �����")
  val nextController: ButtonController = ButtonController(R.id.nextBtn, { onNext() }, "�������� �������")

  val answerController: TextViewController = TextViewController(R.id.answerTextView, "", Visibility.Collapsed)
  val hintController: TextViewController = TextViewController(R.id.hintTextView, "", Visibility.Visible)

  val questionController: TextViewController = TextViewController(R.id.questionTextView)
  val gendersController: List<ButtonController> = listOf(derController, dasController, dieController)
  val answerButtons: List<ButtonController> = listOf(rightController, wrongController)

  val markErrorButton: ButtonController = ButtonController(R.id.markErrorBtn, markError, "Mark Error")
  val genderButtons = mapOf(Pair(Gender.Feminine, dieController), Pair(Gender.Masculine, derController), Pair(Gender.Neuter, dasController))

  protected var currentScore: Float = 1.0f
  private val genderAnswer = (example.entityId as? GermanWordId)?.gender

  fun checkGender(gender: Gender): Boolean {
    val success = genderAnswer == gender
    if (!success) currentScore /= 2
    return success
  }

  fun submitGender(gender: Gender) {
    val success = checkGender(gender)
    if (success) {
      gendersController.forEach { b -> b.visible.value = Visibility.Collapsed }
      onNext()
    } else {
      genderButtons[gender]!!.enabled.value = false
    }
  }

  fun submitAnswer(success: Boolean) {
    if (!success) currentScore /= 2
    result.value = FlowItemResult(success, success, true, !success, currentScore, null)
  }

  fun onNext() {
    nextController.visible.value = Visibility.Collapsed
    answerButtons.forEach { b -> b.visible.value = Visibility.Visible }
    hintController.title.value = example.entityId.toString()
    hintController.visible.value = Visibility.Visible
    answerController.visible.value = Visibility.Visible
  }

  init {
    val shouldGuessGender = genderAnswer != null
    answerButtons.forEach { b -> b.visible.value = Visibility.Collapsed }
    nextController.visible.value = (!shouldGuessGender).toVisibilityCollapsed()
    gendersController.forEach { b -> b.visible.value = shouldGuessGender.toVisibilityCollapsed () }

    questionController.spannedTitle.value = Html.fromHtml(example.text)
    answerController.title.value = example.meanings.joinToString("; ")
    hintController.visible.value = Visibility.Collapsed
  }
}