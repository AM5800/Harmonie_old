package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.controllers.defaultControls.TextViewController
import am5800.harmonie.model.*
import am5800.harmonie.model.util.Property
import am5800.harmonie.viewBinding.BindableView
import am5800.harmonie.viewBinding.ReflectionBindableController
import android.widget.ListView

class TextPartController(private val part: TextPart, scoreCalc: TextPartScoreCalculator) {
  val score: String = "�������: " + Math.round (scoreCalc.calculate(part) * 100.0).toString () + "%"
  val body: String = part.text
}

class TextController(private val textsProvider: TextsProvider,
                     private val lifetime: Lifetime,
                     private val scoreCalc: TextPartScoreCalculator,
                     private val progress: TextProgress
) : ReflectionBindableController(R.layout.text) {
  val texts: List<Text> = textsProvider.texts
  val title: TextViewController = TextViewController(R.id.title, "")
  val currentParts: Property<List<TextPartController>> = Property(emptyList())
  val focusedPart: Property<Int> = Property(0)

  fun openText(textId: String): TextController {
    val text = textsProvider.texts.firstOrNull () { text -> text.id == textId } ?: throw Exception("Text $textId not found")
    val parts = text.parts.map { part -> TextPartController(part, scoreCalc) }
    title.title.value = text.title
    currentParts.value = parts
    focusedPart.value = progress.loadProgress(text)
    focusedPart.bindNotNull(lifetime, { i -> progress.saveProgress(text, i) })
    return this
  }

  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    super.bind(view, bindingLifetime)

    val listView = view.getChild<ListView>(R.id.listview)
    currentParts.bindNotNull (lifetime, { parts ->
      listView.adapter = TextAdapter(view.activity, parts.toTypedArray())
    })

    listView.setSelection(focusedPart.value!!)
    bindingLifetime.addAction {
      focusedPart.value = listView.firstVisiblePosition
    }
  }
}


