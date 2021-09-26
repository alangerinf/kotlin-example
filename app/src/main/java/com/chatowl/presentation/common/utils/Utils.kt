package com.chatowl.presentation.common.utils

import android.content.Context
import android.util.Log
import android.util.Patterns
import com.chatowl.R
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import android.media.MediaRecorder
import java.io.File


const val SERVER_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"
private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
private const val YEAR_MONTH_DAY_FORMAT = "yyyy-MM-dd"
private const val DAY_MONTH_ABBREVIATED_FORMAT = "d MMM"
const val ABBREVIATED_MONTH_DAY_YEAR_FORMAT = "MMM d, yyyy"
const val MONTH_DAY_COMMA_YEAR_FORMAT = "MMMM dd, yyyy"
const val ABBREVIATED_MONTH_DAY_YEAR_HOUR_MINUTES_AM_PM = "MMM d, yyyy – HH:mm a"

fun getDateFromServerDateString(dateString: String, serverTimeZone: Boolean = false): Date {
    val dateFormat = try {
        SimpleDateFormat(SERVER_FORMAT, Locale.getDefault())
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    if (serverTimeZone) {
        dateFormat?.timeZone = TimeZone.getTimeZone("UTC")
    }
    return dateFormat?.parse(dateString) ?: Date()
}

fun getDateFromString(dateString: String, format: String): Date {
    val dateFormat = try {
        SimpleDateFormat(format, Locale.getDefault())
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    return dateFormat?.parse(dateString) ?: Date()
}

fun getRemainingTimeInTrial(context: Context, futureDateString: String): Pair<Boolean, String> {
    return try {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        val futureDate = getDateFromServerDateString(futureDateString)
        calendar.time = futureDate
        val nextTime = calendar.timeInMillis

        val diff = (nextTime - now).coerceAtLeast(0)
        val secondsLeft = diff.div(1000).toInt()
        val minutesLeft = secondsLeft.div(60)
        val hoursLeft = minutesLeft.div(60)
        val daysLeft = hoursLeft.div(24)

        when {
            daysLeft == 1 -> {
                Pair(true, context.resources.getString(R.string.tomorrow))
            }
            daysLeft > 1 -> {
                Pair(
                    daysLeft < 5,
                    context.resources.getQuantityString(
                        R.plurals.format_in_days,
                        daysLeft,
                        daysLeft
                    )
                )
            }
            hoursLeft > 0 -> {
                Pair(
                    true,
                    context.resources.getQuantityString(
                        R.plurals.format_in_hours,
                        hoursLeft,
                        hoursLeft
                    )
                )
            }
            else -> {
                Pair(
                    true,
                    context.resources.getString(R.string.today).decapitalize(Locale.ROOT)
                )
            }
        }
    } catch (e: Exception) {
        Pair(true, "...")
    }
}

fun getRemainingDaysInTrial(daysLeft: Int, context: Context): String {
    return when {
        daysLeft == 0 -> {
            context.resources.getString(R.string.today).decapitalize(Locale.ROOT)
        }
        daysLeft == 1 -> {
            context.resources.getString(R.string.tomorrow)
        }
        else -> {
            context.resources.getQuantityString(
                R.plurals.format_in_days,
                daysLeft,
                daysLeft
            )
        }
    }
}

fun getChatMessageHeaderDescriptiveDate(context: Context, dateString: String): String {
    return try {
        val stringDate = getDateFromString(dateString, YEAR_MONTH_DAY_FORMAT)
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.time = stringDate
        val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
        return when {
            currentDay == dateDay -> {
                context.getString(R.string.today)
            }
            currentDay.minus(dateDay) == 1 -> {
                context.getString(R.string.yesterday)
            }
            else -> {
                changeDateFormat(dateString, YEAR_MONTH_DAY_FORMAT, DAY_MONTH_ABBREVIATED_FORMAT)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        dateString
    }
}

fun getStringTimestampForMedia(): String {
    return getStringTimestampFromDate(requestedFormat = DATE_TIME_FORMAT)
}

fun getMicrophoneAvailable(context: Context): Boolean {
    val recorder = MediaRecorder()
    recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
    recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
    recorder.setOutputFile(File(context.cacheDir, "MediaUtil#micAvailTestFile").absolutePath)
    var available = true
    try {
        recorder.prepare()
        recorder.start()
    } catch (exception: java.lang.Exception) {
        available = false
    }
    recorder.release()
    return available
}

fun getStringTimestampFromDate(
    date: Date? = null,
    requestedFormat: String = SERVER_FORMAT,
    serverTimeZone: Boolean = false
): String {
    val calendarDate = if (date == null) {
        val calendar = Calendar.getInstance()
        calendar.time
    } else {
        date
    }
    val format = SimpleDateFormat(requestedFormat, Locale.getDefault())
    if (serverTimeZone) {
        format.timeZone = TimeZone.getTimeZone("UTC")
    }
    return format.format(calendarDate)
}

fun getTimeFromServerDate(serverDate: String): String {
    return try {
        val originalFormat = SimpleDateFormat(SERVER_FORMAT, Locale.getDefault())
        val targetFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = originalFormat.parse(serverDate)
        targetFormat.format(date ?: "")
    } catch (e: Exception) {
        Log.e("Utils", "Can't parse $serverDate")
        e.printStackTrace()
        "Just now"
    }
}

fun changeDateFormat(dateString: String, from: String, to: String): String {
    val originalFormat = SimpleDateFormat(from, Locale.getDefault())
    val targetFormat = SimpleDateFormat(to, Locale.getDefault())
    val date = originalFormat.parse(dateString)
    val formattedDate = targetFormat.format(date ?: "")
    return formattedDate
}

fun getFormattedDouble(number: Double?): String {
    number?.let {
        return DecimalFormat("0.#", DecimalFormatSymbols(Locale.US)).format(number)
    }
    return "-"
}

fun getFormattedDoubleTwoDecimals(number: Double?): String {
    number?.let {
        return DecimalFormat("0.##", DecimalFormatSymbols(Locale.US)).format(number)
    }
    return "-"
}

fun getServerFormatDate(): String {
    val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    return serverDateFormat.format(calendar.time)
}

fun getShortFormatDate(dateString: String): String {
    return changeDateFormat(dateString, SERVER_FORMAT, YEAR_MONTH_DAY_FORMAT)
}

fun getDayFormatDate(dateString: String): String {
    val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val targetFormat = SimpleDateFormat("EEEE d, MMMM yyyy", Locale.getDefault())
    val date = originalFormat.parse(dateString)
    val formattedDate = targetFormat.format(date ?: "")
    return formattedDate
}

fun getAbbreviatedDate(dateString: String): String {
    val originalFormat = SimpleDateFormat(SERVER_FORMAT, Locale.getDefault())
    val targetFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    val date = originalFormat.parse(dateString)
    val formattedDate = targetFormat.format(date ?: "")
    return formattedDate
}

fun getAbbreviatedDateFromShortDate(dateString: String): String {
    val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val targetFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    val date = originalFormat.parse(dateString)
    val formattedDate = targetFormat.format(date ?: "")
    return formattedDate
}

fun getTimeFromDate(dateString: String): String {
    val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    val targetFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = originalFormat.parse(dateString)
    val formattedDate = targetFormat.format(date ?: "")
    return formattedDate
}

fun getTripDuration(startDateString: String, endDateString: String): Int {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    val startDate = format.parse(startDateString) ?: Date()
    val endDate = format.parse(endDateString) ?: Date()
    val diff: Long = endDate.time - startDate.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return minutes.toInt()
}

fun getTextForPausedAt(context: Context, startDateString: String): String {
    val format = SimpleDateFormat(SERVER_FORMAT, Locale.getDefault())
    val startDate = format.parse(startDateString) ?: Date()

    val totalDays = getDaysPast(startDate)
    val days = totalDays % 7
    val weeks = totalDays / 7
    when{
        weeks > 0 -> {
            return context.resources.getQuantityString(
                R.plurals.format_paused_in_weeks_ago,
                weeks.toInt(),
                weeks.toInt()
            )
        }
        days == 0L -> {
            return context.resources.getString(R.string.plan_paused_today)
        }
        else-> {
            return context.resources.getQuantityString(
                R.plurals.format_paused_in_days_ago,
                days.toInt(),
                days.toInt()
            )
        }
    }
}

fun getPointTimeStamp(trackStartTime: String, timeInMillis: Long): Float {
    val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    val startDateInMillis =
        serverDateFormat.parse(trackStartTime)?.time ?: System.currentTimeMillis()
    val differenceInMilliseconds = timeInMillis - startDateInMillis
    val differenceInSeconds = differenceInMilliseconds / 1000F
    return differenceInSeconds
}

fun getHourMinutesStringForDate(date: Date? = null): String {
    val time = date ?: Calendar.getInstance().time
    val formatTime: DateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatTime.format(time)
}

fun getNextAvailableTime(context: Context, repeatLimitInSeconds: Int, lastPlayed: String): String {
    val calendar = Calendar.getInstance()
    val now = calendar.timeInMillis
    val date = getDateFromServerDateString(lastPlayed)
    calendar.time = date
    calendar.add(Calendar.SECOND, repeatLimitInSeconds)
    val nextTime = calendar.timeInMillis
    val diff = (nextTime - now).coerceAtLeast(0)
    val secondsLeft = diff.div(1000).toInt()
    val minutesLeft = secondsLeft.div(60)
    val hoursLeft = minutesLeft.div(60)
    val daysLeft = hoursLeft.div(24)

    return when {
        daysLeft == 1 -> {
            context.resources.getString(R.string.tomorrow)
        }
        daysLeft > 1 -> {
            context.resources.getQuantityString(
                R.plurals.format_in_days,
                daysLeft,
                daysLeft
            )
        }
        hoursLeft > 0 -> {
            context.resources.getQuantityString(
                R.plurals.format_in_hours,
                hoursLeft,
                hoursLeft
            )
        }
        else -> {
            context.resources.getQuantityString(
                R.plurals.format_in_minutes,
                minutesLeft,
                minutesLeft
            )
        }
    }
}

fun getDaysPast(date: Date): Long {
    val calendar = Calendar.getInstance()
    val now = calendar.timeInMillis

    val diff = (now - date.time).coerceAtLeast(0)

    return (diff / 86400000)
}

fun String.isEmailValid(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}