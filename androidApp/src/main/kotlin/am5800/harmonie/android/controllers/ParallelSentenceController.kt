package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.ReadonlyProperty
import am5800.common.utils.properties.onChangeNotNull
import am5800.harmonie.android.R
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.controllers.util.bindOnClick
import am5800.harmonie.android.controllers.util.bindText
import am5800.harmonie.android.controllers.util.bindVisibility
import am5800.harmonie.android.controllers.util.bindVisibilityInverted
import am5800.harmonie.android.viewBinding.*
import am5800.harmonie.app.model.repetition.LearnScore
import am5800.harmonie.app.vm.ParallelSentenceViewModel
import am5800.harmonie.app.vm.ToggleableWordViewModel
import am5800.harmonie.app.vm.WordViewModel
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import org.apmem.tools.layouts.FlowLayout

class ParallelSentenceController(lifetime: Lifetime,
                                 flowContentController: FlowController,
                                 private val vm: ParallelSentenceViewModel) : ControllerWithMenu, ActivityConsumer {
  private var activity: Activity? = null

  override fun setActivity(activity: Activity, lifetime: Lifetime) {
    lifetime.execute {
      this.activity = activity
      lifetime.addAction { this.activity = null }
    }
  }

  override val menuItems: ReadonlyProperty<List<MenuItem>> = Property(lifetime, vm.reportCommands.map { SimpleMenuItem(it) }.filterIsInstance<MenuItem>())

  override val id: Int = R.layout.parallel_sentence
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    super.bind(view, bindingLifetime)

    val answer = view.getChild<TextView>(R.id.answer)
    answer.bindText(bindingLifetime, view, vm.answer)
    answer.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)

    val showTranslationBtn = view.getChild<Button>(R.id.showTranslationBtn)
    showTranslationBtn.bindOnClick(bindingLifetime, { vm.next() })
    showTranslationBtn.bindText(bindingLifetime, view, vm.continueBtnText)
    showTranslationBtn.bindVisibilityInverted(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)

    val score1 = view.getChild<Button>(R.id.score1Btn)
    val score2 = view.getChild<Button>(R.id.score2Btn)
    val score3 = view.getChild<Button>(R.id.score3Btn)

    score1.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)
    score2.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)
    score3.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)

    score1.bindText(bindingLifetime, view, vm.score1Text)
    score2.bindText(bindingLifetime, view, vm.score2Text)
    score3.bindText(bindingLifetime, view, vm.score3Text)

    score1.bindOnClick(bindingLifetime, { vm.submit(1) })
    score2.bindOnClick(bindingLifetime, { vm.submit(2) })
    score3.bindOnClick(bindingLifetime, { vm.submit(3) })

    val flowLayout = view.getChild<FlowLayout>(R.id.question)
    vm.question.onChange(bindingLifetime, {
      flowLayout.removeAllViews()
      it.newValue.forEach { childVm ->
        val wordView = TextView(view.activity)
        setupWordView(wordView, childVm, bindingLifetime)
        flowLayout.addView(wordView)
      }
    })
  }

  private fun setupWordView(wordView: TextView, childVm: WordViewModel, bindingLifetime: Lifetime) {
    wordView.text = childVm.text

    if (childVm is ToggleableWordViewModel) {
      wordView.setOnClickListener({ childVm.toggle() })

      childVm.state.onChange(bindingLifetime, {
        if (it.newValue == LearnScore.Good) wordView.setTextColor(Color.BLACK)
        else wordView.setTextColor(Color.RED)
      })
    }

    val layoutParams = FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT)
    layoutParams.setMargins(0, 8, 0, 8)
    if (childVm.needSpaceAfter) layoutParams.rightMargin = 16
    wordView.layoutParams = layoutParams

    wordView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25.0f)
  }

  init {
    vm.activationRequested.subscribe(lifetime, { flowContentController.setContent(this) })
    vm.help.onChangeNotNull(lifetime, {
      val alertDialog = AlertDialog.Builder(activity!!).create()
      alertDialog.setTitle("Help")
      alertDialog.setMessage(it)
      alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { dialogInterface, i -> dialogInterface.dismiss() })
      alertDialog.show()
    })
  }
}

