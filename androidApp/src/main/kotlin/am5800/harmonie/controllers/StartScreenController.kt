package am5800.harmonie.controllers

import Lifetime
import am5800.harmonie.R
import am5800.harmonie.ViewOpener
import am5800.harmonie.controllers.defaultControls.ButtonController
import am5800.harmonie.model.EntityScheduler
import am5800.harmonie.model.FlowManager
import am5800.harmonie.viewBinding.BindableView
import am5800.harmonie.viewBinding.ReflectionBindableController
import android.widget.ArrayAdapter
import android.widget.ListView
import org.joda.time.DateTime
import org.joda.time.Minutes

class StartScreenController(
        flowController: FlowManager,
        private val textController: TextController,
        statsController: StatsController,
        private val scheduler: EntityScheduler, private val opener: ViewOpener) : ReflectionBindableController(R.layout.start_screen) {
  private val innerItems = textController.texts
  val items: List<String> = innerItems.map { part -> part.title }

  val openWordsButton: ButtonController = ButtonController(R.id.openWordsBtn, {
    flowController.start(Minutes.minutes(10).toStandardDuration())
  }, createButtonTitle())

  val statsButton: ButtonController = ButtonController(R.id.graph, {
    opener.bringToFront(statsController)
  }, "статистика")

  private fun createButtonTitle(): String {
    val now = DateTime()
    return "слова (${scheduler.getAllScheduledItems().filter { it.dueDate <= now }.map { it.entity }.count()})"
  }

  fun textClicked(index: Int) {
    val id = innerItems[index].id
    opener.bringToFront(textController.openText(id))
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


