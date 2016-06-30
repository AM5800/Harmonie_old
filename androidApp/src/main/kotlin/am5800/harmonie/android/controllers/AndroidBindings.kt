package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.properties.Property
import am5800.common.utils.properties.ReadonlyProperty
import am5800.harmonie.android.Visibility
import am5800.harmonie.android.toAndroidVisibility
import am5800.harmonie.android.viewBinding.UIThreadRunner
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView


fun View.bindVisibility(lifetime: Lifetime, uiThreadRunner: UIThreadRunner, property: Property<Boolean>, invisibleValue: Visibility) {
  property.onChange(lifetime, {
    uiThreadRunner.runOnUiThread {
      if (it.newValue) this.visibility = Visibility.Visible.toAndroidVisibility()
      else this.visibility = invisibleValue.toAndroidVisibility()
    }
  })
}

fun TextView.bindText(lifetime: Lifetime, uiThreadRunner: UIThreadRunner, property: ReadonlyProperty<String>) {
  property.onChange(lifetime, { args ->
    uiThreadRunner.runOnUiThread {
      this.text = args.newValue
    }
  })
}

fun Button.bind(lifetime: Lifetime, uiThreadRunner: UIThreadRunner, text: ReadonlyProperty<String>, onClick: () -> Unit) {
  this.bindText(lifetime, uiThreadRunner, text)
  this.bindOnClick(lifetime, onClick)
}

fun View.bindEnabled(lifetime: Lifetime, uiThreadRunner: UIThreadRunner, property: Property<Boolean>) {
  property.onChange(lifetime, {
    uiThreadRunner.runOnUiThread {
      this.isEnabled = it.newValue
    }
  })
}

fun CheckBox.bindCheckedTwoWay(lifetime: Lifetime, uiThreadRunner: UIThreadRunner, property: Property<Boolean>) {
  property.onChange(lifetime, {
    uiThreadRunner.runOnUiThread {
      this.isChecked = it.newValue
    }
  })

  this.setOnClickListener { property.value = this.isChecked }
}

fun Button.bindOnClick(lifetime: Lifetime, action: () -> Unit) {
  // TODO: multiple events handlers?
  this.setOnClickListener { action() }
  lifetime.addAction { this.setOnClickListener(null) }
}


