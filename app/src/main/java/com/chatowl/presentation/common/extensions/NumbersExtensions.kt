package com.chatowl.presentation.common.extensions

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import com.chatowl.R


fun Int.dpToPx(): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), Resources.getSystem().displayMetrics).toInt()
}

fun Int.secondsToDescriptiveString(context: Context): String {
    return if (this < 60) {
        context.resources.getQuantityString(R.plurals.format_seconds_abbreviation, this, this)
    } else {
        context.resources.getQuantityString(R.plurals.format_minutes_abbreviation, this.div(60), this.div(60))
    }
}

fun Int.secondsToRepeatLimitDescriptiveString(context: Context): String {

    val minutes = this.div(60)
    val hours = minutes.div(60)
    val days = hours.div(24)

    return when {
        days > 0 -> {
            context.resources.getQuantityString(R.plurals.format_days, days, days)
        }
        hours > 0 -> {
            context.resources.getQuantityString(R.plurals.format_hours, hours, hours)
        }
        else -> {
            context.resources.getQuantityString(R.plurals.format_minutes, minutes, minutes)
        }
    }
}

fun Int.secondsToMinuteSecondsString(): String {
    val minutes = this.div(60)
    val minutesString = minutes.toString()

    val seconds = this.minus(minutes * 60)
    var secondsString = seconds.toString()
    if(secondsString.length == 1) secondsString = "0$secondsString"

    return "$minutesString:$secondsString"
}