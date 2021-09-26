package com.chatowl.data.entities.toolbox

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ToolboxMoodMeter(
    var moodId: String = "",
    var moodLabel: String = "",
    val intensities: List<String> = emptyList(),
    var inputs: Int = -1
): Parcelable {
    @ExperimentalStdlibApi
    fun getMode(): MoodMeterMode {
        return when (moodLabel.lowercase()) {
            MoodMeterMode.HOPE.toString().lowercase() -> MoodMeterMode.WORRY
            MoodMeterMode.WORRY.toString().lowercase() -> MoodMeterMode.WORRY
            MoodMeterMode.ANGER.toString().lowercase() -> MoodMeterMode.ANGER
            MoodMeterMode.LONELINESS.toString().lowercase() -> MoodMeterMode.DEPRESSION
            MoodMeterMode.DEPRESSION.toString().lowercase() -> MoodMeterMode.DEPRESSION
            MoodMeterMode.NERVOUSNESS.toString().lowercase() -> MoodMeterMode.NERVOUSNESS
            MoodMeterMode.ANXIETY.toString().lowercase() -> MoodMeterMode.ANXIETY
            else -> MoodMeterMode.OTHER
        }
    }
}

enum class MoodMeterMode {
    HOPE,
    WORRY,
    ANGER,
    LONELINESS,
    DEPRESSION,
    NERVOUSNESS,
    ANXIETY,
    OTHER
}