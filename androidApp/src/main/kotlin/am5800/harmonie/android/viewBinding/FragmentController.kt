package am5800.harmonie.android.viewBinding

import am5800.common.utils.properties.ReadonlyProperty
import am5800.harmonie.app.vm.SimpleCommand

interface MenuItem {
  val title: ReadonlyProperty<String>
  fun onClick()
}

class SimpleMenuItem(override val title: ReadonlyProperty<String>, private val action: () -> Unit) : MenuItem {
  override fun onClick() {
    action()
  }

  constructor(command: SimpleCommand) : this(command.title, command.execute)
}

interface ControllerWithMenu : BindableController {
  val menuItems: ReadonlyProperty<List<MenuItem>>
}

interface FragmentController : BindableController {
  fun tryClose(): Boolean {
    return true
  }
}