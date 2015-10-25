package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.controllers.TextAdapter
import am5800.harmonie.ControllerRegistry
import am5800.harmonie.viewBinding.BindableView
import am5800.harmonie.viewBinding.ReflectionBindableController
import am5800.harmonie.controllers.defaultControls.ButtonController
import am5800.harmonie.controllers.defaultControls.TextViewController
import am5800.harmonie.model.*
import am5800.harmonie.model.util.Property
import android.widget.ListView

public class TextPartController(private val part: TextPart, scoreCalc: TextPartScoreCalculator) {
    val score: String = "Лексика: " + Math.round (scoreCalc.calculate(part) * 100.0).toString () + "%"
    val body: String = part.text
}

public class TextController(private val textsProvider: TextsProvider,
                           private val lifetime: Lifetime,
                           private val scoreCalc: TextPartScoreCalculator,
                           private val progress: TextProgress,
                           private val vmRegistry: ControllerRegistry
) : ReflectionBindableController(R.layout.text) {
    val texts: List<Text> = textsProvider.texts
    public val title: TextViewController = TextViewController(R.id.title, "")
    public val currentParts: Property<List<TextPartController>> = Property(emptyList())
    public val focusedPart: Property<Int> = Property(0)

    public fun open(textId: String): Boolean {
        val text = textsProvider.texts.firstOrNull () { text -> text.id == textId } ?: return false
        val parts = text.parts.map { part -> TextPartController(part, scoreCalc) }
        title.title.value = text.title
        currentParts.value = parts
        focusedPart.value = progress.loadProgress(text)
        focusedPart.bindNotNull(lifetime, { i -> progress.saveProgress(text, i) })
        vmRegistry.bringToFront(this)
        return true
    }

    override fun bind(view: BindableView, bindingLifetime: Lifetime) {
        super.bind(view, bindingLifetime)

        val listView = view.getChild<ListView>(R.id.listview)
        currentParts.bindNotNull (lifetime, { parts ->
            listView.setAdapter(TextAdapter(view.activity, parts.toTypedArray(), lifetime))
        })

        listView.setSelection(focusedPart.value!!)
        bindingLifetime.addAction {
            focusedPart.value = listView.getFirstVisiblePosition()
        }
    }
}


