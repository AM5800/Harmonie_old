package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.ReadonlyProperty
import am5800.common.utils.properties.onChangeNotNull
import am5800.harmonie.android.R
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.controllers.util.*
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
import android.widget.LinearLayout
import android.widget.ListView
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

    val answerGroup = view.getChild<LinearLayout>(R.id.answerGroup)
    answerGroup.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)

    val showTranslationBtn = view.getChild<Button>(R.id.showTranslationBtn)
    showTranslationBtn.bindOnClick(bindingLifetime, { vm.next() })
    showTranslationBtn.bindText(bindingLifetime, view, vm.continueBtnText)
    showTranslationBtn.bindVisibilityInverted(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)

    val btn0 = view.getChild<Button>(R.id.btn0)
    val btn1 = view.getChild<Button>(R.id.btn1)
    val btn2 = view.getChild<Button>(R.id.btn2)

    btn0.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)
    btn1.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)
    btn2.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)

    btn0.bindText(bindingLifetime, view, vm.btn0Text)
    btn1.bindText(bindingLifetime, view, vm.btn1Text)
    btn2.bindText(bindingLifetime, view, vm.btn2Text)

    btn0.bindOnClick(bindingLifetime, { vm.submit(0) })
    btn1.bindOnClick(bindingLifetime, { vm.submit(1) })
    btn2.bindOnClick(bindingLifetime, { vm.submit(2) })

    val flowLayout = view.getChild<FlowLayout>(R.id.question)
    vm.question.onChange(bindingLifetime, {
      flowLayout.removeAllViews()
      it.newValue.forEach { childVm ->
        val wordView = TextView(view.activity)
        setupWordView(wordView, childVm, bindingLifetime)
        flowLayout.addView(wordView)
      }
    })

    ListViewController.bind(view.getChild<ListView>(R.id.meanings), bindingLifetime, view, vm.problemWords, {
      ProblemWordController(it)
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
    layoutParams.setMargins(0, 0, 0, 16)
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

