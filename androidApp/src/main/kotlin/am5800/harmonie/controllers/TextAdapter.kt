package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.controllers.defaultControls.ButtonController
import am5800.harmonie.model.Lifetime
import am5800.harmonie.toVisible
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class TextAdapter(context: Context, private val values: Array<TextPartController>) : ArrayAdapter<TextPartController>(context, R.layout.text_row, values) {
  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val rootView = inflater.inflate(R.layout.text_row, parent, false)

    val vm = values[position]

    val score = rootView.findViewById(R.id.scoreTextView) as TextView
    val body = rootView.findViewById(R.id.body) as TextView

    score.text = vm.score
    body.text = vm.body

    return rootView
  }
}

fun Button.bind(vm: ButtonController, lifetime: Lifetime) {
  vm.title.bindNotNull(lifetime, { text = it })
  vm.enabled.bindNotNull(lifetime, { isEnabled = it })
  vm.visible.bindNotNull(lifetime, { visibility = it.toVisible() })
  setOnClickListener { b -> vm.clicked () }
}