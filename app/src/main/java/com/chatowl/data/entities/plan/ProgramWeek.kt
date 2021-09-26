package com.chatowl.data.entities.plan

import android.os.Parcelable
import com.chatowl.data.entities.home.ProgramActivity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProgramWeek(
    val week: Int,
    val name: String,
    val description: String,
    var status: String = ProgramWeekStatus.TO_DO.name,
    val thumbnailUrl: String,
    val weekActivities: List<ProgramActivity>,
    var isExpanded: Boolean = false
) : Parcelable

enum class ProgramWeekStatus {
    DONE,
    IN_PROGRESS,
    TO_DO
}