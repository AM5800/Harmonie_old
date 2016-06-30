package am5800.harmonie.app.model.features.flow

import am5800.common.utils.EnumerableDistribution
import am5800.common.utils.Lifetime
import am5800.common.utils.SequentialLifetime
import am5800.common.utils.properties.NullableProperty
import am5800.common.utils.properties.NullableReadonlyProperty
import am5800.harmonie.app.model.DebugOptions


class FlowManager(lifetime: Lifetime, val providers: Collection<FlowItemProvider>, val debugOptions: DebugOptions) {
  private val flowLifetime = SequentialLifetime(lifetime)
  private val _currentFlow = NullableProperty<Flow>(lifetime, null)
  val currentFlow: NullableReadonlyProperty<Flow>
    get() = _currentFlow

  fun start(distribution: EnumerableDistribution<FlowItemCategory>) {
    val lt = flowLifetime.current
    lt.addAction { _currentFlow.value = null }
    val flow = Flow(lt, providers, debugOptions, distribution)
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