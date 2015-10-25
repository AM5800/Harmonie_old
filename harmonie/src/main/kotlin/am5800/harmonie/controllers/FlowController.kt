package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.ControllerRegistry
import am5800.harmonie.viewBinding.BindableView
import am5800.harmonie.viewBinding.ReflectionBindableController
import am5800.harmonie.controllers.defaultControls.TextViewController
import am5800.harmonie.model.*
import am5800.harmonie.model.util.Property
import am5800.harmonie.model.util.SequentialLifetime
import android.widget.LinearLayout
import org.joda.time.*
import org.joda.time.format.PeriodFormatterBuilder

public class FlowController(parentLifetime: Lifetime,
                           private val flowManager: FlowManager,
                           private val contentManagers: List<ContentManagerControllerProvider>,
                           private val controllerRegistry: ControllerRegistry) : ReflectionBindableController(R.layout.flow_fragment) {
    private val lifetime = SequentialLifetime(parentLifetime)
    val currentItem: Property<FlowItemController> = Property(null)
    val score : TextViewController = TextViewController(R.id.scoreTextView, "")
    val schedule : TextViewController = TextViewController(R.id.scheduleTextView, "")

    public val timeLeft: TextViewController = TextViewController(R.id.timeLeftTextView, "", Visibility.Visible, true)

    private fun continueFlow(flow: Flow) {
        val current = flow.currentItem
        var vm = if (current == null)
            EmptyController()
        else
            contentManagers.map { it.tryGetController (current) }.firstOrNull { it != null }

        if (vm == null) throw Exception("Can not handle entity: $current")

        currentItem.value = vm
        val lt = lifetime.next()
        if (lt != null) {
            vm.result.bindNotNull(lt, { answer ->
                val nextDueDate = flow.next(answer)
                if (nextDueDate == null) schedule.title.value = "error"
                else schedule.title.value = formatDate(nextDueDate)
                continueFlow(flow)
            })
        }
    }

    override fun bind(view: BindableView, bindingLifetime: Lifetime) {
        super.bind(view, bindingLifetime)
        val placeholder = view.getChild<LinearLayout>(R.id.placeholder);

        currentItem.bindNotNull(bindingLifetime, {
            placeholder.removeAllViews()
            placeholder.addView(view.createViewAndBind(it, bindingLifetime))
        })
    }

    private fun formatDate(nextDueDate: DateTime): String {
        val now = DateTime()
        val sourceString = "След. повтор через "
        val dueDate = nextDueDate.plus(Seconds.ONE)

        val minutes = Minutes.minutesBetween(now, dueDate).getMinutes()
        if (minutes < 60) {
            return sourceString + pluralize(minutes, "минуту", "минуты", "минут")
        }

        val hours = Hours.hoursBetween(now, dueDate).getHours()
        if (hours < 24) {
            return sourceString + pluralize(hours, "час", "часа", "часов")
        }

        val days = Days.daysBetween(now, dueDate).getDays()
        if (days < 7) {
            return sourceString + pluralize(days, "день", "дня", "дней")
        }

        val weeks = Weeks.weeksBetween(now, dueDate).getWeeks()
        val months = Months.monthsBetween(now, dueDate).getMonths()

        if (months < 1) {
            return sourceString + pluralize(weeks, "неделю", "недели", "недель")
        }

        else return sourceString + pluralize(months, "месяц", "месяца", "месяцев")
    }

    private fun pluralize(i: Int, one: String, two: String, five: String): String {
        val result = i.toString() + " ";

        val mod = i.mod(10)
        if (i > 10 && i < 20) return result + five
        if (mod == 1) return result + one
        if (mod == 2 || mod == 3 || mod == 4) return result + two
        return result + five
    }

    init {
        flowManager.started.subscribe(parentLifetime, {
            controllerRegistry.bringToFront(this)
            it.score.bindNotNull(it.lifetime, {
                val p = if (it.total == 0) 0.0f else it.right.toFloat () / it.total
                score.title.value = "${it.right} / ${it.total} ($p)"
            })

            it.timeLeft.bindNotNull(it.lifetime, {
                val formatter = PeriodFormatterBuilder()
                        .minimumPrintedDigits(2)
                        .printZeroAlways()
                        .appendMinutes()
                        .appendSuffix(":")
                        .appendSeconds()
                timeLeft.title.value = it.toPeriod ().toString(formatter.toFormatter())
            })

            if (it.flowType.contains (FlowType.TimeTrial)) timeLeft.visible.value = Visibility.Visible
            else timeLeft.visible.value = Visibility.Collapsed

            schedule.title.value = ""
            continueFlow(it)
        })
    }
}