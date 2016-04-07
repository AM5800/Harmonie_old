package am5800.harmonie.app.vm

import am5800.common.utils.Lifetime
import am5800.common.utils.Signal

interface ViewModel {
  val activationRequested: Signal<Unit>
  val closeRequested: Signal<Unit>
}

open class ViewModelBase(lifetime: Lifetime) : ViewModel {
  override val activationRequested: Signal<Unit> = Signal(lifetime)
  override val closeRequested: Signal<Unit> = Signal(lifetime)
}