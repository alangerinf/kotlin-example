package com.chatowl.data.entities.journey

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import com.chatowl.R
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Journey(
    val moodLabel: JourneyItemMood,
    val moodColor: String = "",
    val values: List<JourneyValue> = emptyList()
): Parcelable {

    enum class JourneyItemMood {
        Anger,
        Anxiety,
        Hope,
        Loneliness,
        Worry,
        Depression,
        Nervousness,
        MoodAverage;

        fun name(ctx: Context): String = when (this) {
            Anger -> ctx.getString(R.string.anger)
            Anxiety -> ctx.getString(R.string.anxiety)
            Loneliness -> ctx.getString(R.string.loneliness)
            Hope, Worry -> ctx.getString(R.string.worry)
            Loneliness,Depression -> ctx.getString(R.string.depression)
            Nervousness -> ctx.getString(R.string.nervousness)
            MoodAverage -> "MoodAverage"
        }


        fun description(ctx: Context): String = when (this) {
            else -> "" // TODO: Replace with real description. waiting design team
        }

        fun color(ctx: Context): Int =
            Color.parseColor(
                ctx.getString(
                    when (this) {
                        Anger -> R.color.emotion_anger
                        Anxiety -> R.color.emotion_anxiety
                        Hope,Worry -> R.color.emotion_hope
                        Loneliness,Depression -> R.color.emotion_loneliness
                        Nervousness -> R.color.emotion_nervousness
                        else -> R.color.dark
                    }
                )
            )

        fun lottieRaw(): Int =
            when (this) {
                Anger -> R.raw.anger_lottie
                Anxiety -> R.raw.anxiety_lottie
                Hope,Worry -> R.raw.hopeful_lottie
                Loneliness,Depression -> R.color.emotion_loneliness
                Nervousness -> R.color.emotion_nervousness
                else -> R.color.emotion_hope
            }


        fun minValueDescription(ctx: Context): String =
            ctx.getString(
                when (this) {
                    Anger -> R.string.emotion_anger_desc_min
                    Anxiety -> R.string.emotion_anxiety_desc_min
                    Hope, Worry -> R.string.emotion_worry_desc_min //check for String.splitInLines()?.get(0)
                                                                    //if it is too long
                    Loneliness, Depression -> R.string.emotion_loneliness_desc_min
                    Nervousness -> R.string.emotion_nervousness_desc_min
                    else -> R.string.emotion_worry_desc_min
                }
            )

        fun maxValueDescription(ctx: Context): String =
            ctx.getString(
                when (this) {
                    Anger -> R.string.emotion_anger_desc_max
                    Anxiety -> R.string.emotion_anxiety_desc_max
                    Hope, Worry -> R.string.emotion_worry_desc_max //check for String.splitInLines()?.get(0)
                                                                    //if it is too long
                    Loneliness,Depression -> R.string.emotion_loneliness_desc_max
                    Nervousness -> R.string.emotion_nervousness_desc_max
                    else -> R.string.emotion_worry_desc_max
                }
            )

    }
}

