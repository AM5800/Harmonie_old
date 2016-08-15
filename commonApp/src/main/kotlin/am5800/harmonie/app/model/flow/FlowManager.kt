package am5800.harmonie.app.model.flow

import am5800.common.utils.EnumerableDistribution
import am5800.common.utils.Lifetime
import am5800.common.utils.SequentialLifetime
import am5800.common.utils.properties.NullableProperty
import am5800.common.utils.properties.NullableReadonlyProperty
import am5800.harmonie.app.model.DebugOptions
import am5800.harmonie.app.model.workspace.TagStatisticsProvider


class FlowManager(lifetime: Lifetime,
                  private val providers: Collection<FlowItemProvider>,
                  private val debugOptions: DebugOptions,
                  private val tagStatisticsProvider: TagStatisticsProvider) {
  private val flowLifetime = SequentialLifetime(lifetime)
  private val _currentFlow = NullableProperty<Flow>(lifetime, null)
  val currentFlow: NullableReadonlyProperty<Flow>
    get() = _currentFlow

  fun start(distribution: EnumerableDistribution<FlowItemTag>) {
    val lt = flowLifetime.current
    lt.addAction { _currentFlow.value = null }
    val flow = Flow(lt, providers, debugOptions, distribution, tagStatisticsProvider)
    _currentFlow.value = flow
    flow.next(0, 0)
  }


  fun stop() {
    flowLifetime.next()
  }

  fun next(successDelta: Int, failureDelta: Int) {
    currentFlow.value!!.next(successDelta, failureDelta)
  }
}