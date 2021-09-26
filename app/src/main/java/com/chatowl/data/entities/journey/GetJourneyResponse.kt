package com.chatowl.data.entities.journey

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chatowl.presentation.journey.JourneyTimeOption
import kotlinx.android.parcel.Parcelize
import java.util.*


@Parcelize
@Entity(tableName = "get_journey_response")
data class GetJourneyResponse(
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0,
    val dateUnit: JourneyTimeOption,
    val firstDate: String,
    val startDate : String,
    val endDate : String,
    val journey: List<Journey> = emptyList()
    ): Parcelable {

    companion object {
        const val ID_DAYS = 1
        const val ID_WEEKS = 2
        const val ID_MONTHS = 3

    }
}