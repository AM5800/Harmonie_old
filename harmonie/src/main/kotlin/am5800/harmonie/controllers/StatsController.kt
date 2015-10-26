package am5800.harmonie.controllers

import am5800.harmonie.ControllerRegistry
import am5800.harmonie.R
import am5800.harmonie.model.*
import am5800.harmonie.model.util.Property
import am5800.harmonie.viewBinding.BindableController
import am5800.harmonie.viewBinding.BindableView
import android.graphics.Color
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.joda.time.DateMidnight
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.LinkedHashMap

public class StatsController(private val histMan: AttemptsHistoryManager,
                             private val controllerRegistry: ControllerRegistry,
                             private val repetitionAlg: RepetitionAlgorithm) : BindableController {
    override fun bind(view: BindableView, bindingLifetime: Lifetime) {
        val graph = view.getChild<GraphView>(R.id.graph)

        graph.legendRenderer.isVisible = true
        fun getColor(level: WordLearnLevel) = when (level) {
            WordLearnLevel.JustStarted -> Color.BLUE
            WordLearnLevel.BarelyKnown -> Color.GREEN
            WordLearnLevel.Confident -> Color.YELLOW
            WordLearnLevel.Known -> Color.MAGENTA
            else -> throw UnsupportedOperationException()
        }

        data.bindNotNull(bindingLifetime, {
            graph.removeAllSeries()
            val dataPoints = WordLearnLevel.values().filter {it != WordLearnLevel.NotStarted}.toMap ({ it }, { arrayListOf<DataPoint>() })

            it.forEachIndexed { i, statPoint ->
                for (level in statPoint.data) {
                    if (level.getKey() == WordLearnLevel.NotStarted) continue
                    val s = dataPoints.get(level.getKey())!!
                    val dataPoint = DataPoint(i.toDouble(), level.getValue().toDouble())
                    s.add(dataPoint)
                }
            }

            for (dataPoint in dataPoints.toList()) {
                val title = titles[dataPoint.first] ?: continue
                val series = dataPoint.second.toTypedArray()
                val graphSeries = LineGraphSeries(series)
                graphSeries.title = title
                graphSeries.color = getColor(dataPoint.first)
                graph.addSeries(graphSeries)
            }
        })
    }

    override val id: Int = R.layout.statistics

    public class StatPoint(public val data: Map<WordLearnLevel, Int>)

    public val data: Property<List<StatPoint>> = Property(emptyList())
    public val titles: Map<WordLearnLevel, String> = WordLearnLevel.values().toMap ({ it }, { it.toString() })
    fun activate() {
        calcData()
        controllerRegistry.bringToFront(this)
    }

    private fun calcData() {
        val now = DateTime()
        val dates = buildDates(now, 7)

        val result = arrayListOf<StatPoint>()

        val words = histMan.getKeys()

        for (date in dates) {
            val map = createNewMap()
            for (word in words) {
                val attemptsToDate = histMan.getAttempts(word).filter { it.date < date }
                val level = repetitionAlg.computeLevel(attemptsToDate)
                map[level] = map[level]!! + 1
            }
            result.add(StatPoint(map))

        }

        data.value = result
    }

    private fun createNewMap(): LinkedHashMap<WordLearnLevel, Int> {
        val result = LinkedHashMap<WordLearnLevel, Int>()
        WordLearnLevel.values().forEach { result.put(it, 0) }
        return result
    }

    private fun buildDates(now: DateTime, days: Int): List<DateTime> {
        val midnight = DateMidnight(now.plus(Days.ONE))
        val result = arrayListOf<DateTime>()
        repeat(days, {
            result.add(midnight.minus(Days.ONE.multipliedBy(days - it)).toDateTime())
        })
        return result
    }
}