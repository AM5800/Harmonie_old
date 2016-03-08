package am5800.harmonie.android.controllers

import am5800.common.utils.Lifetime
import am5800.common.utils.Property
import am5800.harmonie.android.toVisible
import am5800.harmonie.app.vm.Visibility
import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.TextView


fun View.bindVisibility(lifetime: Lifetime, activity: Activity, property: Property<Visibility>) {
  property.bind(lifetime, { args ->
    val value = args.newValue
    if (value != null) {
      activity.runOnUiThread { this.visibility = value.toVisible() }
    }
  })
}

fun TextView.bindText(lifetime: Lifetime, activity: Activity, property: Property<String>) {
  property.bind(lifetime, { args ->
    activity.runOnUiThread {
      this.text = args.newValue ?: ""
    }
  })
}

fun <T> TextView.bindText(lifetime: Lifetime, activity: Activity, property: Property<T>, mapper: (T) -> String) {
  property.bind(lifetime, { args ->
    val value = args.newValue

    activity.runOnUiThread {
      if (value == null) this.text = ""
      else this.text = mapper.invoke(value)
    }
  })
}

fun Button.bindOnClick(lifetime: Lifetime, action: () -> Unit) {
  this.setOnClickListener { action() }
  lifetime.addAction { this.setOnClickListener(null) }
}


