package com.chatowl.presentation.common.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.chatowl.R
import kotlinx.android.synthetic.main.layout_app_preferences_item.view.*

class OutlinedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    var strokeColor: Int = 0

    init {
        strokeColor = ContextCompat.getColor(context, R.color.outlineColor)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //draw outline
        val paint: Paint = paint
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = paint.textSize * 0.03F
        val color_tmp = paint.color
        setTextColor(strokeColor)
        super.onDraw(canvas)
        //restore
        setTextColor(color_tmp)
        paint.style = Paint.Style.FILL
    }

}