package com.chatowl.presentation.journey

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.widget.TextView
import com.chatowl.R
import com.chatowl.data.entities.journey.Journey
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF


class CustomMarker(context: Context?) : MarkerView(context, R.layout.custom_marker) {

    var lineChart: LineChart? = null
    var tView_moodName: TextView
    var tView_moodValue: TextView

    init {
        tView_moodName = findViewById(R.id.tView_moodName)
        tView_moodValue = findViewById(R.id.tView_moodValue)
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF? {
        val offset = MPPointF(posX, posY)
        offset.x = offset.x - width / 2 - 360
        offset.y = offset.y - height / 2
        return offset
    }

    var currentEntry: Entry? = null

    override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
        val p = Paint()
        p.color = Color.WHITE
        p.strokeWidth = 20f
        p.style = Paint.Style.STROKE

        val circleHeight = 22.0f
        canvas?.drawCircle(posX, posY, circleHeight, p)

        (currentEntry?.data as? Journey.JourneyItemMood)?.let { mood ->
            val p2 = Paint()
            p2.color = mood.color(context)
            p2.strokeWidth = 1f
            p2.style = Paint.Style.FILL_AND_STROKE
            canvas?.drawCircle(posX, posY, 12f, p2)
        }

        val f = getOffsetForDrawingAtPoint(posX, posY)
        f?.let {
            canvas?.translate(it.x, it.y)
            draw(canvas)
        }
    }



    override fun refreshContent(entry: Entry?, highlight: Highlight?) {
        var moodName = ""
        (entry?.data as? Journey.JourneyItemMood)?.let { mood ->
            moodName = mood.name(context)
        }
        entry?.let { entry ->
            val formattedY = if (entry.y.toInt() > 0) "+${entry.y.toInt()}" else entry.y.toInt().toString()
            tView_moodName.text = moodName
            tView_moodValue.text = formattedY
            super.refreshContent(entry, highlight)
        }
        currentEntry = entry
    }

}