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


public class GermanRecallController(private val example : RenderedExample, markError : () -> Unit) : ReflectionBindableController(R.layout.word_recall_view), FlowItemController {
    override val result: Property<FlowItemResult> = Property(null)
    public val dasController: ButtonController = ButtonController(R.id.dasBtn, { submitGender(Gender.Neuter, dasController) }, "das")
    public val derController: ButtonController = ButtonController(R.id.derBtn, { submitGender(Gender.Masculine, derController) }, "der")
    public val dieController: ButtonController = ButtonController(R.id.dieBtn, { submitGender(Gender.Feminine, dieController) }, "die")
    public val rightController: ButtonController = ButtonController(R.id.rightBtn, { submitAnswer(true) }, "�����")
    public val wrongController: ButtonController = ButtonController(R.id.wrongBtn, { submitAnswer(false) }, "�� �����")
    public val nextController: ButtonController = ButtonController(R.id.nextBtn, { onNext() }, "�������� �������")

    public val answerController: TextViewController = TextViewController(R.id.answerTextView, "", Visibility.Collapsed)
    public val hintController: TextViewController = TextViewController(R.id.hintTextView, "", Visibility.Visible)

    public val questionController: TextViewController = TextViewController(R.id.questionTextView)
    public val gendersController: List<ButtonController> = listOf(derController, dasController, dieController)
    public val answerButtons: List<ButtonController> = listOf(rightController, wrongController)

    public val markErrorButton : ButtonController = ButtonController(R.id.markErrorBtn, markError, "Mark Error")

    protected var currentScore : Float = 1.0f
    private val genderAnswer = (example.entityId as? GermanWordId)?.gender

    fun checkGender(gender: Gender): Boolean {
        val success = genderAnswer == gender
        if (!success) currentScore /= 2
        return success
    }

    fun submitGender(gender: Gender, btn: ButtonController) {
        val success = checkGender(gender)
        if (success) {
            gendersController.forEach { b -> b.visible.value = Visibility.Collapsed }
            onNext()
        } else {
            btn.enabled.value = false
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
        answerController.title.value = example.meanings.join("; ")
        hintController.visible.value = Visibility.Collapsed
    }
}