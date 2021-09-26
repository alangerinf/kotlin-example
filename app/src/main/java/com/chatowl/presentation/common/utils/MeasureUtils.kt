package com.chatowl.presentation.common.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics

object MeasureUtils {

    fun getScreenHeightPercentage(context: Context, percentage: Double): Int {
        // Get screen height
        val displayMetrics = DisplayMetrics()
        val height = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = context.display
            display?.getRealMetrics(displayMetrics)
            displayMetrics.heightPixels
        } else {
            (context as Activity).windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
        // Return corresponding percentage of screen height
        return height.times(percentage).toInt()
    }
}
