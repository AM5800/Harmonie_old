package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.controllers.defaultControls.ButtonController
import am5800.harmonie.model.EntityScheduler
import am5800.harmonie.model.FlowManager
import am5800.harmonie.model.Lifetime
import am5800.harmonie.viewBinding.BindableView
import am5800.harmonie.viewBinding.ReflectionBindableController
import android.widget.ArrayAdapter
import android.widget.ListView
import org.joda.time.DateTime
import org.joda.time.Minutes

public class StartScreenController(
        flowController: FlowManager,
        private val textController: TextController,
        statsVm: StatsController,
        private val scheduler: EntityScheduler) : ReflectionBindableController(R.layout.start_screen) {
    private val innerItems = textController.texts
    val items: List<String> = innerItems.map { part -> part.title }

    public val openWordsButton: ButtonController = ButtonController(R.id.openWordsBtn, {
        flowController.start(Minutes.minutes(10).toStandardDuration())
    }, createButtonTitle())

    public val statsButton: ButtonController = ButtonController(R.id.graph, {
        statsVm.activate()
    }, "статистика")

    private fun createButtonTitle() : String {
        val now = DateTime()
        return "слова (${scheduler.getAllScheduledItems().filter { it.dueDate <= now }.map { it.entity }.count()})"
    }

    fun textClicked(index: Int) {
        val id = innerItems[index].id
        textController.open(id)
    }

    override fun bind(view: BindableView, bindingLifetime: Lifetime) {
        super.bind(view, bindingLifetime)
        val textsView = view.getChild<ListView>(R.id.textParts)
        textsView.adapter = ArrayAdapter(view.activity, android.R.layout.simple_list_item_1, items)
        textsView.setOnItemClickListener({ a, b, c, d ->
            textClicked(c)
        });
    }

    override fun onActivated() {
        openWordsButton.title.value = createButtonTitle()
    }
}


