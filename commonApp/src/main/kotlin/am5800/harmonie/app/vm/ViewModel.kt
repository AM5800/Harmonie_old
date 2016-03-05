package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Signal


interface ViewModel {
  fun onActivated()
  val activationRequired: Signal<Unit>
}

open class ViewModelBase(lifetime: Lifetime) : ViewModel {
  override fun onActivated() {

  }

  override val activationRequired: Signal<Unit> = Signal(lifetime)
}