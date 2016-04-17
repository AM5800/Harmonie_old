package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.android.R
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.viewBinding.*
import am5800.harmonie.app.model.features.repetition.LearnScore
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

  override val menuItems = Property(lifetime, vm.reportCommands.map { SimpleMenuItem(it) }.filterIsInstance<MenuItem>())

  override val id: Int = R.layout.parallel_sentence
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    super.bind(view, bindingLifetime)

    val answer = view.getChild<TextView>(R.id.answer)
    answer.bindText(bindingLifetime, view, vm.answer)
    answer.bindVisibility(bindingLifetime, view, vm.answerGroupVisibility, Visibility.Collapsed)

    val nextBtn = view.getChild<Button>(R.id.continueBtn)
    nextBtn.bindOnClick(bindingLifetime, { vm.next() })
    nextBtn.bindText(bindingLifetime, view, vm.continueBtnText)

    val flowLayout = view.getChild<FlowLayout>(R.id.question)
    vm.question.onChange(bindingLifetime, {
      flowLayout.removeAllViews()
      for (childVm in it.newValue!!) {
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
    if (childVm.needSpaceBefore) layoutParams.leftMargin = 16
    wordView.layoutParams = layoutParams

    wordView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25.0f)
  }

  init {
    vm.activationRequested.subscribe(lifetime, { flowContentController.setContent(this) })
    vm.help.onChangeNotNull(lifetime, {
      val alertDialog = AlertDialog.Builder(activity!!).create();
      alertDialog.setTitle("Help");
      alertDialog.setMessage(it);
      alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { dialogInterface, i -> dialogInterface.dismiss() })
      alertDialog.show();
    })
  }
}

