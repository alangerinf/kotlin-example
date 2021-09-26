package com.chatowl.presentation.journey

import androidx.lifecycle.LiveData
import com.chatowl.data.helpers.DateUtils
import com.chatowl.data.helpers.DateUtils.formatTo
import com.chatowl.data.helpers.DateUtils.isToday
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter
import java.util.*

class CustomFormatter(private val timeOption: JourneyTimeOption, var digits: Int) : DefaultAxisValueFormatter(digits) {

    override fun getFormattedValue(value: Float): String {
        val date = Date((value* DateUtils.CONSTANT_MILLIS_TO_DAYS).toLong())
        return when {
            isToday(date) -> {
                "Today"
            }
            timeOption ==  JourneyTimeOption.months-> {
                date.formatTo("MM/yyyy")
            }
            else -> {
                date.formatTo("MM/dd/yyyy")
            }
        }
    }
}