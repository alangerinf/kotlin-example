package com.chatowl.data.helpers

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    const val CONSTANT_MILLIS_TO_DAYS = 86400000
    const val COMPLETE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_AMERICAN_FORMAT = "MM/dd/yyyy"
    const val DATE_AMERICAN_SHORT_FORMAT = "MM/dd/yy"
    const val DATE_AMERICAN_SHORT_FORMAT_NO_YEAR = "MM/dd"
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    const val DATE_TIME_WEEK_FORMAT = "EEE, d MMM yyyy HH:mm"
    const val DATE_WEEK_FORMAT = "EEE, d MMM yyyy"
    const val HOUR_12_FORMAT = "h:mm a"
    const val HOUR_24_FORMAT = "HH:mm"
    const val MONTH_DAY_FORMAT = "MMMM d, yyyy"
    const val WEEK_FORMAT = "EEE"

    const val DATE_DAY_NUMBER = "d"
    const val DATE_DAY_STRING = "EEE"
    const val DATE_MONTH_STRING = "MMM"
    const val DATE_YEAR_STRING = "yyyy"

    fun getDate(dayOfMonth: Int, month: Int, year: Int, hour: Int = 0, minute: Int = 0): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        return cal.time
    }

    fun isToday(date: Date): Boolean {
        val cal = Calendar.getInstance()
        val calToday = Calendar.getInstance()
        val today = Date()
        cal.time = date
        calToday.time = today
        return cal.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR) &&
                cal.get(Calendar.YEAR) == calToday.get(Calendar.YEAR)
    }

    fun parseFromString(date: String, pattern: String): Date? {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.parse(date)
    }

    fun Date.formatTo(pattern: String): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(this)
    }

    fun Date.getDayInt(): Int {
        val cal = Calendar.getInstance()
        cal.time = this
        return cal.get(Calendar.DAY_OF_MONTH)
    }

}