package am5800.harmonie.controllers

import am5800.harmonie.R
import am5800.harmonie.toVisible
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import am5800.harmonie.controllers.TextPartController
import am5800.harmonie.controllers.TextController
import am5800.harmonie.controllers.defaultControls.ButtonController
import am5800.harmonie.model.Lifetime

public class TextAdapter(context: Context, private val values: Array<TextPartController>, private val lifetime: Lifetime) : ArrayAdapter<TextPartController>(context, R.layout.text_row, values) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val inflater = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = inflater.inflate(R.layout.text_row, parent, false)

        val vm = values[position]

        val score = rootView.findViewById(R.id.scoreTextView) as TextView
        val body = rootView.findViewById(R.id.body) as TextView

        score.setText(vm.score)
        body.setText(vm.body)

        return rootView
    }
}

public fun Button.bind(vm: ButtonController, lifetime: Lifetime) {
    vm.title.bindNotNull(lifetime, { setText(it) })
    vm.enabled.bindNotNull(lifetime, { setEnabled (it) })
    vm.visible.bindNotNull(lifetime, { setVisibility(it.toVisible()) })
    setOnClickListener { b -> vm.clicked () }
}