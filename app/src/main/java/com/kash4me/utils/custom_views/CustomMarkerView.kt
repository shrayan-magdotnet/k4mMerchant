package com.kash4me.utils.custom_views

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.kash4me.R

class CustomMarkerView(context: Context, layoutResource: Int) :
    MarkerView(context, layoutResource) {

    override fun refreshContent(e: Entry, highlight: Highlight) {
        // Override this method to update the content of your custom MarkerView.
        // Here, you can set the text of the TextViews based on the chart data or any other logic.

        // For example, if you have TextViews with IDs textView1 and textView2 in your custom layout:
        val textView1 = findViewById<TextView>(R.id.textView1)
        val textView2 = findViewById<TextView>(R.id.textView2)

        // Set the text for the TextViews based on the chart data or any other logic
        textView1.text = "Value: ${e.y}"
        textView2.text = "Label: ${e.data}"

        super.refreshContent(e, highlight)
    }

}
