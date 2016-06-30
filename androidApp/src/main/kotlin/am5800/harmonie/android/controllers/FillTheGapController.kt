package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.ReadonlyProperty
import am5800.harmonie.android.R
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.viewBinding.BindableView
import am5800.harmonie.android.viewBinding.ControllerWithMenu
import am5800.harmonie.android.viewBinding.MenuItem
import am5800.harmonie.android.viewBinding.SimpleMenuItem
import am5800.harmonie.app.vm.FillTheGapViewModel
import android.widget.Button
import android.widget.TextView


class FillTheGapController(private val viewModel: FillTheGapViewModel,
                           flowContentController: FlowController,
                           lifetime: Lifetime) : ControllerWithMenu {
  override val menuItems: ReadonlyProperty<List<MenuItem>> = Property(lifetime, viewModel.reportCommands.map { SimpleMenuItem(it) }.filterIsInstance<MenuItem>())

  override val id = R.layout.fill_the_gap
  override fun bind(view: BindableView, bindingLifetime: Lifetime) {
    val variant1 = view.getChild<Button>(R.id.variant1)
    val variant2 = view.getChild<Button>(R.id.variant2)
    val variant3 = view.getChild<Button>(R.id.variant3)
    val variant4 = view.getChild<Button>(R.id.variant4)

    variant1.bindVisibility(bindingLifetime, view, viewModel.variantsVisible, Visibility.Collapsed)
    variant2.bindVisibility(bindingLifetime, view, viewModel.variantsVisible, Visibility.Collapsed)
    variant3.bindVisibility(bindingLifetime, view, viewModel.variantsVisible, Visibility.Collapsed)
    variant4.bindVisibility(bindingLifetime, view, viewModel.variantsVisible, Visibility.Collapsed)

    viewModel.variants.onChange(bindingLifetime, {
      val vs = listOf(variant1, variant2, variant3, variant4)
      val vms = viewModel.variants.value

      val zipped = vs.zip(vms)
      for ((button, vm) in zipped) {
        button.bindEnabled(bindingLifetime, view, vm.enabled)
        button.text = vm.title
        button.bindOnClick(bindingLifetime, { vm.signal.fire(Unit) })
      }
    })

    val sentence = view.getChild<TextView>(R.id.sentence)
    val translation = view.getChild<TextView>(R.id.translation)

    sentence.bindText(bindingLifetime, view, viewModel.sentence)
    translation.bindText(bindingLifetime, view, viewModel.translation)
  }

  init {
    viewModel.activationRequested.subscribe(lifetime, {
      flowContentController.setContent(this)
    })
  }
}